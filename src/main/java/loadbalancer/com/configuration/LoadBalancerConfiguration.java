package loadbalancer.com.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class LoadBalancerConfiguration {

    @Bean
    public List<String> backendUrls() {
        return Arrays.asList(
                "http://localhost:8081",
                "http://localhost:8082"
        );
    }

    @Bean
    public List<String> activeServers() {
        List<String> activeServers = new CopyOnWriteArrayList<>();
        activeServers.addAll(backendUrls());
        return activeServers;
    }

    @Bean
    public AtomicInteger counter() {
        return new AtomicInteger(0);
    }


}
