package com.jkc.microservices.composite.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@SpringBootApplication
@ComponentScan("com.jkc.microservices")
public class ProductCompositeServiceApplication implements WebFluxConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ProductCompositeServiceApplication.class, args);
    }

//    @Bean
//    RestTemplate restTemplate() {
//        return new RestTemplate();
//    }

}
