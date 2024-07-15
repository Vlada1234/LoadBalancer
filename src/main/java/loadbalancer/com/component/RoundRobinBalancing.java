package loadbalancer.com.component;

import loadbalancer.com.service.BalancingStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
@Component
public class RoundRobinBalancing implements BalancingStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);


    @Override
    public String chooseServer(List<String> servers) {
        int serverIndex = counter.getAndUpdate(current -> (current + 1) % servers.size());
        return servers.get(serverIndex);
    }
}
