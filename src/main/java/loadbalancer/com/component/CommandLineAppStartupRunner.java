package loadbalancer.com.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.logging.Logger;

public class CommandLineAppStartupRunner implements ApplicationRunner {
    @Value("${health.check.url}")
    private String healthCheckUrl;

    @Value("${health.check.period}")
    private long healthCheckPeriod;

    private static final Logger logger = Logger.getLogger(CommandLineAppStartupRunner.class.getName());

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("health.check.url")) {
            healthCheckUrl = args.getOptionValues("health.check.url").get(0);
        }
        if (args.containsOption("health.check.period")) {
            healthCheckPeriod = Long.parseLong(args.getOptionValues("health.check.period").get(0));
        }
        logger.info("Health Check URL: " + healthCheckUrl);
        logger.info("Health Check Period: " + healthCheckPeriod);
    }
}
