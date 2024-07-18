package loadbalancer.com.dto;

import loadbalancer.com.component.StrategyNames;

public class StrategyRequest {
    private StrategyNames strategy;

    public StrategyRequest() {
    }

    public StrategyRequest(StrategyNames strategy) {
        this.strategy = strategy;
    }

    public StrategyNames getStrategy() {
        return strategy;
    }

    public void setStrategy(StrategyNames strategy) {
        this.strategy = strategy;
    }
}
