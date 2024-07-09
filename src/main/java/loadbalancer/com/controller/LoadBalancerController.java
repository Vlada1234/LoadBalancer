package loadbalancer.com.controller;


import jakarta.servlet.http.HttpServletRequest;
import loadbalancer.com.component.LoggingFilter;
import loadbalancer.com.service.HealthCheckService;
import loadbalancer.com.service.RequestForwardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;


@RestController
public class LoadBalancerController {

    private final LoggingFilter loggingFilter;
    private final RequestForwardingService requestForwardingService;
    private final HealthCheckService healthCheckService;

    @Autowired
    public LoadBalancerController(LoggingFilter loggingFilter, RequestForwardingService requestForwardingService, HealthCheckService healthCheckService) {
        this.loggingFilter = loggingFilter;
        this.requestForwardingService = requestForwardingService;
        this.healthCheckService = healthCheckService;
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


