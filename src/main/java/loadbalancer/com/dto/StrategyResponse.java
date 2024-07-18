package loadbalancer.com.dto;

public class StrategyResponse {
    private String strategy;


    public StrategyResponse() {
    }

    public StrategyResponse(String strategy) {
        this.strategy = strategy;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
