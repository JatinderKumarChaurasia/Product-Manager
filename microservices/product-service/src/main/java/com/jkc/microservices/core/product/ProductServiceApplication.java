package com.jkc.microservices.core.product;

import com.jkc.microservices.core.product.models.ProductEntity;
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
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

@SpringBootApplication
@ComponentScan("com.jkc.microservices")
public class ProductServiceApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceApplication.class);
    private final MongoOperations mongoTemplate;

    @Autowired
    public ProductServiceApplication(MongoOperations mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(ProductServiceApplication.class, args);
        String mongoDbHost = configurableApplicationContext.getEnvironment().getProperty("spring.data.mongodb.host");
        String mongoDbPort = configurableApplicationContext.getEnvironment().getProperty("spring.data.mongodb.port");
        LOGGER.info("Connected to MongoDb: {}:{}", mongoDbHost, mongoDbPort);
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
        IndexResolver indexResolver = new MongoPersistentEntityIndexResolver(mappingContext);
        IndexOperations indexOperations = mongoTemplate.indexOps(ProductEntity.class);
        indexResolver.resolveIndexFor(ProductEntity.class).forEach(indexOperations::ensureIndex);
    }
}
