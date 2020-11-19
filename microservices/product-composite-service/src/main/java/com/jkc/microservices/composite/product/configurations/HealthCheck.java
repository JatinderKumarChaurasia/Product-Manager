package com.jkc.microservices.composite.product.configurations;

import com.jkc.microservices.composite.product.services.ProductCompositeIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class HealthCheck {

    private final ProductCompositeIntegration productCompositeIntegration;

    @Autowired
    public HealthCheck(ProductCompositeIntegration productCompositeIntegration) {
        this.productCompositeIntegration = productCompositeIntegration;
    }

    @Bean
    ReactiveHealthContributor coreServices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();
        registry.put("products", productCompositeIntegration::getProductHealth);
        registry.put("recommendations", productCompositeIntegration::getRecommendationHealth);
        registry.put("reviews", productCompositeIntegration::getReviewHealth);
        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
