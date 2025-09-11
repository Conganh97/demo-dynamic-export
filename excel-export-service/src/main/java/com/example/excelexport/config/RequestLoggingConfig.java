package com.example.excelexport.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger("REQUEST_LOGGER");

    @Bean
    public AbstractRequestLoggingFilter requestLoggingFilter() {
        return new AbstractRequestLoggingFilter() {
            @Override
            protected void beforeRequest(HttpServletRequest request, String message) {
                String threadName = Thread.currentThread().getName();
                if (request.getRequestURI().contains("/api/excel/export")) {
                    logger.info("INCOMING REQUEST - Thread:" + threadName + ", Method: " + request.getMethod()
                            + ", URI: " + request.getRequestURI() + ", Params: " + request.getQueryString())
                    ;
                }
            }

            @Override
            protected void afterRequest(HttpServletRequest request, String message) {
                String threadName = Thread.currentThread().getName();
                if (request.getRequestURI().contains("/api/excel/export")) {
                    logger.info("📤 REQUEST COMPLETED - Thread:" + threadName + ", Method: " + request.getMethod()
                            + ", URI: " + request.getRequestURI() + ", Params: " + request.getQueryString());
                }
            }
        };
    }
}
