package loadbalancer.com.component;

import loadbalancer.com.service.BalancingStrategy;

import java.util.List;

public class RandomNumberBalancing implements BalancingStrategy {

    @Override
    public StrategyNames getStrategyName() {
        return StrategyNames.RANDOMNUMBER;
    }

    @Override
    public String chooseServer(List<String> servers) {
        int serverIndex = (int) (Math.random() * ((servers.size())));
        return servers.get(serverIndex);
    }
}
