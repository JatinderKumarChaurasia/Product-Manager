package com.jkc.microservices.core.product.mappers;

import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.core.product.models.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mappings(value = {
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    ProductEntity productApiToProductEntity(Product product);

    @Mappings(value = {
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Product productEntityToProductApi(ProductEntity productEntity);
}
