package loadbalancer.com.component;

import loadbalancer.com.service.BalancingStrategy;

import java.util.List;

public class RandomNumberBalancing implements BalancingStrategy {

    @Override
    public String getStrategyName() {
        return "RandomNumber";
    }

    @Override
    public String chooseServer(List<String> servers) {
        int serverIndex = (int) (Math.random() * ((servers.size())));
        return servers.get(serverIndex);
    }
}
