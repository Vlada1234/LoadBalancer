package loadbalancer.com.dto;

import loadbalancer.com.component.StrategyNames;

public class StrategyResponse {
    private StrategyNames strategy;


    public StrategyResponse() {
    }

    public StrategyResponse(StrategyNames strategy) {
        this.strategy = strategy;
    }

    public StrategyNames getStrategy() {
        return strategy;
    }

    public void setStrategy(StrategyNames strategy) {
        this.strategy = strategy;
    }
}
