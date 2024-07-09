package loadbalancer.com.service;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Service
public class RequestForwardingService {

    private final List<String> activeServers;
    private final AtomicInteger counter;

    private static final Logger logger = Logger.getLogger(RequestForwardingService.class.getName());

    @Autowired
    public RequestForwardingService(List<String> activeServers, AtomicInteger counter) {
        this.activeServers = activeServers;
        this.counter = counter;
    }

    public String forwardRequestToBackend(HttpServletRequest request, String requestBody, RequestMethod method) {
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
                            logger.info("Response from server: " + response.getStatusLine());
                            return backendResponse;
                        }
                    } catch (IOException e) {
                        logger.info("Request to " + backendUri + " failed. Trying the next server.");
                        e.printStackTrace();
                    }
                }
            }
            return "No backend servers available";
        }
    public String extractRequestBody(HttpServletRequest request) {
        try (Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    }
