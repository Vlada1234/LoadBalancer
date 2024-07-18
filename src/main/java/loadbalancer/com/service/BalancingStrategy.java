package loadbalancer.com.service;

import loadbalancer.com.component.StrategyNames;
import java.util.List;

public interface BalancingStrategy {
    StrategyNames getStrategyName();
    String chooseServer(List<String> servers);
}
