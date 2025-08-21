package com.gym.workload.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingFilter.class);
    private static final String TRANSACTION_ID_HEADER = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Check for incoming transaction ID from upstream service
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put("transactionId", transactionId);

        long startTime = System.currentTimeMillis();
        log.info("WORKLOAD-SERVICE Transaction START [{} {}] - txId={}",
                request.getMethod(), request.getRequestURI(), transactionId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("WORKLOAD-SERVICE Transaction END - status={} duration={}ms txId={}",
                    response.getStatus(), duration, transactionId);
            MDC.clear();
        }
    }
}