package loadbalancer.com.component;

public class StrategyResponse {
    private String message;


    public StrategyResponse() {
    }

    public StrategyResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
