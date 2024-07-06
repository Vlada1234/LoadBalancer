package loadbalancer.com.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class LoadBalancerController {

    @Value("${health.check.url}")
    private String healthCheckUrl;

    @Value("${health.check.period}")
    private long healthCheckPeriod;

    private static final String[] BACKEND_URLS = {
            "http://localhost:8081",
            "http://localhost:8082"
    };
    private final List<String> activeServers = new CopyOnWriteArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        activeServers.addAll(Arrays.asList(BACKEND_URLS));


        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();
        taskScheduler.scheduleAtFixedRate(this::performHealthChecks, healthCheckPeriod);
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public CompletableFuture<String> handleGetRequest(HttpServletRequest request, @RequestHeader Map<String, String> headers) {
        logRequestDetails(request, headers);
        return CompletableFuture.supplyAsync(() -> forwardRequestToBackend(request, null, RequestMethod.GET));
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    public CompletableFuture<String> handlePostRequest(HttpServletRequest request, @RequestHeader Map<String, String> headers) {
        logRequestDetails(request, headers);
        String requestBody = extractRequestBody(request);
        return CompletableFuture.supplyAsync(() -> forwardRequestToBackend(request, requestBody, RequestMethod.POST));
    }

    private void logRequestDetails(HttpServletRequest request, Map<String, String> headers) {
        String clientIp = request.getRemoteAddr();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String protocol = request.getProtocol();

        System.out.println("Received request from " + clientIp);
        System.out.println(method + " " + uri + " " + protocol);

        headers.forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });

        System.out.println();
    }

     private String forwardRequestToBackend(HttpServletRequest request, String requestBody, RequestMethod method) {
        synchronized (activeServers) {
            int attempts = activeServers.size();
            for (int i = 0; i < attempts; i++) {
                int serverIndex = counter.getAndUpdate(current -> (current + 1) % activeServers.size());
                if (serverIndex >= activeServers.size()) {
                    continue;
                }
                String backendUri = activeServers.get(serverIndex) + request.getRequestURI();

                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    CloseableHttpResponse response = null;
                    if (method == RequestMethod.GET) {
                        HttpGet httpGet = new HttpGet(backendUri);
                        response = httpClient.execute(httpGet);
                    } else if (method == RequestMethod.POST) {
                        HttpPost httpPost = new HttpPost(backendUri);
                        if (requestBody != null && !requestBody.isEmpty()) {
                            httpPost.setEntity(new StringEntity(requestBody));
                        }
                        response = httpClient.execute(httpPost);
                    }
                    if (response != null) {
                        String backendResponse = EntityUtils.toString(response.getEntity());
                        System.out.println("Response from server: " + response.getStatusLine());
                        return backendResponse;
                    }
                } catch (IOException e) {
                    System.out.println("Request to " + backendUri + " failed. Trying the next server.");
                    e.printStackTrace();
                }
            }
        }
        return "No backend servers available";
    }

    private String extractRequestBody(HttpServletRequest request) {
        try (Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }



    @Scheduled(fixedRateString = "${health.check.period}")
    private void performHealthChecks() {
        for (String url : BACKEND_URLS) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url + healthCheckUrl);
                CloseableHttpResponse response = httpClient.execute(httpGet);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    if (!activeServers.contains(url)) {
                        activeServers.add(url);
                        System.out.println("Server added back to the pool: " + url);
                    }
                } else {
                    activeServers.remove(url);
                    System.out.println("Server removed from the pool: " + url);
                }
            } catch (IOException e) {
                activeServers.remove(url);
                System.out.println("Server removed from the pool due to exception: " + url);
            }
        }
    }
}


