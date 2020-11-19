package com.jkc.microservices.composite.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.event.EventModel;
import org.junit.jupiter.api.Test;

import static com.jkc.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


class IsSameEventTests {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testEventObjectMapperCompare() throws JsonProcessingException {
        EventModel<Integer, Product> eventModel = new EventModel<>(EventModel.TYPE.CREATE, 1, new Product(1, "name", 1, null));
        EventModel<Integer, Product> eventModel1 = new EventModel<>(EventModel.TYPE.CREATE, 1, new Product(1, "name", 1, null));
        EventModel<Integer, Product> eventModel2 = new EventModel<>(EventModel.TYPE.DELETE, 1, null);
        EventModel<Integer, Product> eventModel3 = new EventModel<>(EventModel.TYPE.CREATE, 1, new Product(2, "name", 1, null));
        String event1Json = objectMapper.writeValueAsString(eventModel);
        assertThat(event1Json, is((sameEventExceptCreatedAt(eventModel1))));
        assertThat(event1Json, not(sameEventExceptCreatedAt(eventModel2)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(eventModel3)));
    }
}
