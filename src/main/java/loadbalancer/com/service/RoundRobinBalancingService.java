package loadbalancer.com.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoundRobinBalancingService implements BalancingStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);


    @Override
    public String chooseServer(List<String> servers) {
        if(servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("Server list cannot be null or empty");
        }
        int serverIndex = counter.getAndUpdate(current -> (current + 1) % servers.size());
        return servers.get(serverIndex);
    }
}
