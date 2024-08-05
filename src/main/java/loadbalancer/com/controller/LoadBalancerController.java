package loadbalancer.com.controller;


import jakarta.servlet.http.HttpServletRequest;
import loadbalancer.com.component.*;
import loadbalancer.com.dto.StrategyRequest;
import loadbalancer.com.dto.StrategyResponse;
import loadbalancer.com.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


@RestController
public class LoadBalancerController {

    private final LoggingFilter loggingFilter;
    private final RequestForwardingService requestForwardingService;
    private final HealthCheckService healthCheckService;
    private final RateLimiter rateLimiter;



    @Autowired
    public LoadBalancerController(LoggingFilter loggingFilter, RequestForwardingService requestForwardingService, HealthCheckService healthCheckService, BalancingStrategy balancingStrategy, RateLimiter rateLimiter) {
        this.loggingFilter = loggingFilter;
        this.requestForwardingService = requestForwardingService;
        this.healthCheckService = healthCheckService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/change-strategy")
    public ResponseEntity<StrategyResponse> changeStrategy(@RequestBody StrategyRequest request) {
        if (!rateLimiter.isAllowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(null);
        }

        RoundRobinBalancing roundRobinBalancing = new RoundRobinBalancing();
        RandomNumberBalancing randomNumberBalancing = new RandomNumberBalancing();

        StrategyNames strategy = request.getStrategy();

        if (roundRobinBalancing.getStrategyName() == strategy) {
            requestForwardingService.setBalancingStrategy(roundRobinBalancing);
            return ResponseEntity.noContent().build();
        } else {
            requestForwardingService.setBalancingStrategy(randomNumberBalancing);
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/current-strategy")
    public  ResponseEntity<StrategyResponse> currentStrategy() {
        if(!rateLimiter.isAllowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(null);
        }

        try {
            RoundRobinBalancing roundRobinBalancing = new RoundRobinBalancing();
            RandomNumberBalancing randomNumberBalancing = new RandomNumberBalancing();

            BalancingStrategy currentStrategy = requestForwardingService.getBalancingStrategy();
            StrategyNames currentStrategyName = currentStrategy.getStrategyName();
            return ResponseEntity.ok(new StrategyResponse(currentStrategyName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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


