package loadbalancer.com.service;

import java.util.List;

public interface BalancingStrategy {
    String getStrategyName();
    String chooseServer(List<String> servers);
}
