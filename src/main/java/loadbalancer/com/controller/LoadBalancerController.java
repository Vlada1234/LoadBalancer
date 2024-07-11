package loadbalancer.com.controller;


import jakarta.servlet.http.HttpServletRequest;
import loadbalancer.com.component.LoggingFilter;
import loadbalancer.com.service.BalancingStrategy;
import loadbalancer.com.service.HealthCheckService;
import loadbalancer.com.service.RequestForwardingService;
import loadbalancer.com.service.RoundRobinBalancingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


@RestController
public class LoadBalancerController {

    private final LoggingFilter loggingFilter;
    private final RequestForwardingService requestForwardingService;
    private final HealthCheckService healthCheckService;
    private final RoundRobinBalancingService roundRobinBalancingService;

    @Autowired
    public LoadBalancerController(LoggingFilter loggingFilter, RequestForwardingService requestForwardingService, HealthCheckService healthCheckService, BalancingStrategy balancingStrategy, RoundRobinBalancingService roundRobinBalancingService) {
        this.loggingFilter = loggingFilter;
        this.requestForwardingService = requestForwardingService;
        this.healthCheckService = healthCheckService;
        this.roundRobinBalancingService = roundRobinBalancingService;
    }

    @GetMapping("/change-strategy")
    public String changeStrategy(@RequestParam String strategy) {
        if ("roundrobin".equalsIgnoreCase(strategy)) {
            requestForwardingService.setBalancingStrategy(roundRobinBalancingService);
            return "Changed to RoundRobin strategy";
        } else {
            return "Unknown strategy";
        }
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


