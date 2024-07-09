package loadbalancer.com.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@Service
public class HealthCheckService {

    @Value("${health.check.url}")
    private String healthCheckUrl;

    private final List<String> backendUrls;
    private final List<String> activeServers;

    private static final Logger logger = Logger.getLogger(HealthCheckService.class.getName());
    @Autowired
    public HealthCheckService(List<String> backendUrls, List<String> activeServers) {
        this.backendUrls = backendUrls;
        this.activeServers = activeServers;
    }

    @Scheduled(fixedRateString = "${health.check.period}")
    public void performHealthChecks() {
        for (String url : backendUrls) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url + healthCheckUrl);
                CloseableHttpResponse response = httpClient.execute(httpGet);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    if (!activeServers.contains(url)) {
                        activeServers.add(url);
                        logger.info("Server added back to the pool: " + url);
                    }
                } else {
                    activeServers.remove(url);
                    logger.info("Server removed from the pool: " + url);
                }
            } catch (IOException e) {
                activeServers.remove(url);
                logger.info("Server removed from the pool due to exception: " + url);
            }
        }
    }
}
