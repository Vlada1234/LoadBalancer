package loadbalancer.com.component;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.logging.Logger;

@Component
@WebFilter("/*")
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logRequestDetails(request);
        filterChain.doFilter(request, response);
    }

    public void logRequestDetails(HttpServletRequest request) {
            String clientIp = request.getRemoteAddr();
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String protocol = request.getProtocol();

        logger.info("Received request from " + clientIp);
        logger.info("Request: " + method + " " + uri + " " + protocol);


        logHeaders(request);

    }

    private void logHeaders(HttpServletRequest request) {
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            logger.info(headerName + ": " + headerValue);
        });
    }
}

