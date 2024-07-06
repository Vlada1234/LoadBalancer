package loadbalancer.com.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class CommandLineAppStartupRunner implements ApplicationRunner {
    @Value("${health.check.url}")
    private String healthCheckUrl;

    @Value("${health.check.period}")
    private long healthCheckPeriod;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("health.check.url")) {
            healthCheckUrl = args.getOptionValues("health.check.url").get(0);
        }
        if (args.containsOption("health.check.period")) {
            healthCheckPeriod = Long.parseLong(args.getOptionValues("health.check.period").get(0));
        }
        System.out.println("Health Check URL: " + healthCheckUrl);
        System.out.println("Health Check Period: " + healthCheckPeriod);
    }
}
