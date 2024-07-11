package loadbalancer.com.service;

import java.util.List;

public interface BalancingStrategy {
    String chooseServer(List<String> servers);
}
