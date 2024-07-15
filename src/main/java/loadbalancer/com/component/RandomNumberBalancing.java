package loadbalancer.com.component;

import loadbalancer.com.service.BalancingStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class RandomNumberBalancing implements BalancingStrategy {

    @Override
    public String chooseServer(List<String> servers) {
        int serverIndex = (int) (Math.random() * ((servers.size())));
        return servers.get(serverIndex);
    }
}
