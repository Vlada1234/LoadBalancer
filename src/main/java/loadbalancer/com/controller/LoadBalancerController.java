package loadbalancer.com.controller;


import jakarta.servlet.http.HttpServletRequest;
import loadbalancer.com.component.LoggingFilter;
import loadbalancer.com.component.RandomNumberBalancing;
import loadbalancer.com.component.RoundRobinBalancing;
import loadbalancer.com.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@RestController
public class LoadBalancerController {

    private final LoggingFilter loggingFilter;
    private final RequestForwardingService requestForwardingService;
    private final HealthCheckService healthCheckService;



    @Autowired
    public LoadBalancerController(LoggingFilter loggingFilter, RequestForwardingService requestForwardingService, HealthCheckService healthCheckService, BalancingStrategy balancingStrategy) {
        this.loggingFilter = loggingFilter;
        this.requestForwardingService = requestForwardingService;
        this.healthCheckService = healthCheckService;
    }

    @PostMapping("/change-strategy")
    public ResponseEntity<Map<String, String>> changeStrategy(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();

        RoundRobinBalancing roundRobinBalancing = new RoundRobinBalancing();
        RandomNumberBalancing randomNumberBalancing = new RandomNumberBalancing();

        String strategy = request.get("strategy");

        if ("roundrobin".equalsIgnoreCase(strategy)) {
            requestForwardingService.setBalancingStrategy(roundRobinBalancing);
            response.put("message", "Changed to RoundRobin strategy") ;
            return ResponseEntity.ok(response);
        } else if("randomnumber".equalsIgnoreCase(strategy)) {
            requestForwardingService.setBalancingStrategy(randomNumberBalancing);
            response.put("message", "Changed to RandomNumber strategy");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Unknown Strategy");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/current-strategy")
    public  ResponseEntity<Map<String, String>> currentStrategy() {
        Map<String, String> response = new HashMap<>();

        RoundRobinBalancing roundRobinBalancing = new RoundRobinBalancing();
        RandomNumberBalancing randomNumberBalancing = new RandomNumberBalancing();

        BalancingStrategy currentStrategy = requestForwardingService.getBalancingStrategy();
        String currentStrategyName = currentStrategy.getStrategyName();
        response.put("message", "Current strategy is: " + currentStrategyName);

        return ResponseEntity.ok(response);

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


