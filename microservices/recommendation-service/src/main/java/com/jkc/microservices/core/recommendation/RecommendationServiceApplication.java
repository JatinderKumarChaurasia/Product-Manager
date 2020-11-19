package com.jkc.microservices.core.recommendation;

import com.jkc.microservices.core.recommendation.models.RecommendationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

@SpringBootApplication
@ComponentScan("com.jkc.microservices")
public class RecommendationServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationServiceApplication.class);

    @Autowired
    ReactiveMongoOperations reactiveMongoOperations;

    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(RecommendationServiceApplication.class, args);

        String mongoDBHost = configurableApplicationContext.getEnvironment().getProperty("spring.data.mongodb.host");
        String mongoDBPort = configurableApplicationContext.getEnvironment().getProperty("spring.data.mongodb.port");
        LOGGER.info("connected to mongodb with host: {} and port:{}", mongoDBHost, mongoDBPort);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = reactiveMongoOperations.getConverter().getMappingContext();
        IndexResolver indexResolver = new MongoPersistentEntityIndexResolver(mappingContext);
        ReactiveIndexOperations reactiveIndexOperations = reactiveMongoOperations.indexOps(RecommendationEntity.class);
        indexResolver.resolveIndexFor(RecommendationEntity.class).forEach(i -> reactiveIndexOperations.ensureIndex(i).block());
    }
}
