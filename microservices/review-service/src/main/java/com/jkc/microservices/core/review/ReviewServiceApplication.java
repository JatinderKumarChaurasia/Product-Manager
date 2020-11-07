package com.jkc.microservices.core.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.jkc.microservices")
public class ReviewServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(ReviewServiceApplication.class, args);
        String mySQLUri = applicationContext.getEnvironment().getProperty("spring.datasource.url");
        LOGGER.info("connected to mysql: {}", mySQLUri);
    }
}
