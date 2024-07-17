package loadbalancer.com.component;

public class StrategyRequest {
    private String strategy;

    public StrategyRequest() {
    }

    public StrategyRequest(String strategy) {
        this.strategy = strategy;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
