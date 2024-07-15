package loadbalancer.com.controller;


import jakarta.servlet.http.HttpServletRequest;
import loadbalancer.com.component.LoggingFilter;
import loadbalancer.com.component.RandomNumberBalancing;
import loadbalancer.com.component.RoundRobinBalancing;
import loadbalancer.com.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


@RestController
public class LoadBalancerController {

    private final LoggingFilter loggingFilter;
    private final RequestForwardingService requestForwardingService;
    private final HealthCheckService healthCheckService;
    private final RoundRobinBalancing roundRobinBalancing;

    private final RandomNumberBalancing randomNumberBalancing;

    @Autowired
    public LoadBalancerController(LoggingFilter loggingFilter, RequestForwardingService requestForwardingService, HealthCheckService healthCheckService, BalancingStrategy balancingStrategy, RoundRobinBalancing roundRobinBalancing, RandomNumberBalancing randomNumberBalancing) {
        this.loggingFilter = loggingFilter;
        this.requestForwardingService = requestForwardingService;
        this.healthCheckService = healthCheckService;
        this.roundRobinBalancing = roundRobinBalancing;
        this.randomNumberBalancing = randomNumberBalancing;
    }

    @GetMapping("/change-strategy")
    public String changeStrategy(@RequestParam String strategy) {
        if ("roundrobin".equalsIgnoreCase(strategy)) {
            requestForwardingService.setBalancingStrategy(roundRobinBalancing);
            return "Changed to RoundRobin strategy";
        } else if("randomnumber".equalsIgnoreCase(strategy)) {
            requestForwardingService.setBalancingStrategy(randomNumberBalancing);
            return "Changed to RandomNumber strategy";
        }
        return "Unknown Strategy";
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public CompletableFuture<String> handleGetRequest(HttpServletRequest request) {
        return CompletableFuture.supplyAsync(() -> requestForwardingService.forwardRequestToBackend(request, null, RequestMethod.GET));
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    public CompletableFuture<String> handlePostRequest(HttpServletRequest request) {
        String requestBody = requestForwardingService.extractRequestBody(request);
        return CompletableFuture.supplyAsync(() -> requestForwardingService.forwardRequestToBackend(request, requestBody, RequestMethod.POST));
    }
}


