package loadbalancer.com.controller;


import jakarta.servlet.http.HttpServletRequest;
import loadbalancer.com.component.*;
import loadbalancer.com.dto.ErrorResponse;
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



    @Autowired
    public LoadBalancerController(LoggingFilter loggingFilter, RequestForwardingService requestForwardingService, HealthCheckService healthCheckService, BalancingStrategy balancingStrategy) {
        this.loggingFilter = loggingFilter;
        this.requestForwardingService = requestForwardingService;
        this.healthCheckService = healthCheckService;
    }

    @PostMapping("/change-strategy")
    public ResponseEntity<ErrorResponse> changeStrategy(@RequestBody StrategyRequest request) {

        RoundRobinBalancing roundRobinBalancing = new RoundRobinBalancing();
        RandomNumberBalancing randomNumberBalancing = new RandomNumberBalancing();


        String strategy = request.getStrategy();

        if (roundRobinBalancing.getStrategyName().equalsIgnoreCase(strategy)) {
            requestForwardingService.setBalancingStrategy(roundRobinBalancing);
            return ResponseEntity.noContent().build();
        } else if(roundRobinBalancing.getStrategyName().equalsIgnoreCase(strategy)) {
            requestForwardingService.setBalancingStrategy(randomNumberBalancing);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Unknown Strategy"));
        }
    }

    @GetMapping("/current-strategy")
    public  ResponseEntity<StrategyResponse> currentStrategy() {

        RoundRobinBalancing roundRobinBalancing = new RoundRobinBalancing();
        RandomNumberBalancing randomNumberBalancing = new RandomNumberBalancing();

        BalancingStrategy currentStrategy = requestForwardingService.getBalancingStrategy();
        String currentStrategyName = currentStrategy.getStrategyName();
        return ResponseEntity.ok(new StrategyResponse(currentStrategyName));
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


