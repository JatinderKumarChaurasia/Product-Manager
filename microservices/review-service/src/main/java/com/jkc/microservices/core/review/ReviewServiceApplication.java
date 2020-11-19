package com.jkc.microservices.core.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@SpringBootApplication
@ComponentScan("com.jkc.microservices")
public class ReviewServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceApplication.class);

    private final Integer connectionPoolSize;

    @Autowired
    public ReviewServiceApplication(@Value("${spring.datasource.maximum-pool-size:10}") int maximumPoolSize) {
        this.connectionPoolSize = maximumPoolSize;
    }

    @Bean
    public Scheduler jobScheduler() {
        LOGGER.info("Creates a jdbcScheduler with connectionPoolSize : {}", connectionPoolSize);
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(ReviewServiceApplication.class, args);
        String mySQLUri = applicationContext.getEnvironment().getProperty("spring.datasource.url");
        LOGGER.info("connected to mysql: {}", mySQLUri);
    }
}
