package com.bank.clientservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
public class AsyncConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // Set timeout for async requests (30 minutes for SSE)
        configurer.setDefaultTimeout(30L * 60 * 1000);

        // Register callable interceptors if needed
        configurer.registerCallableInterceptors();

        // Register deferred result interceptors if needed
        configurer.registerDeferredResultInterceptors();
    }
}