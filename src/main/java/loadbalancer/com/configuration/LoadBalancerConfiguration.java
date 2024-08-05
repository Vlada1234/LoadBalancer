package loadbalancer.com.configuration;

import loadbalancer.com.component.LeakyBucketRateLimiter;
import loadbalancer.com.service.BalancingStrategy;
import loadbalancer.com.component.RandomNumberBalancing;
import loadbalancer.com.component.RoundRobinBalancing;
import loadbalancer.com.service.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class LoadBalancerConfiguration {

    @Value("${balancing.strategy}")
    private String balancingStrategy;

    @Value("${rate.limiter.capacity}")
    private long capacity;

    @Value("${rate.limiter.rate.per.second}")
    private long ratePerSecond;

    @Bean
    public RateLimiter rateLimiter() {
        return new LeakyBucketRateLimiter(capacity, ratePerSecond);
    }

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

    @Bean
    public BalancingStrategy balancingStrategy() {
        if ("roundrobin".equalsIgnoreCase(balancingStrategy)) {
            return new RoundRobinBalancing();
        } else if ("randomnumber".equalsIgnoreCase(balancingStrategy)) {
            return new RandomNumberBalancing();
        } else {
            throw new IllegalArgumentException("Unknown balancing strategy: " + balancingStrategy);
        }
    }
}




