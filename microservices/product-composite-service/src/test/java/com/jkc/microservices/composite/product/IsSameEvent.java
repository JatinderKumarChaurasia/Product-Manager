package com.jkc.microservices.composite.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkc.microservices.api.event.EventModel;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IsSameEvent extends TypeSafeMatcher<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsSameEvent.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventModel<?, ?> expectedEvent;

    private IsSameEvent(EventModel<?, ?> expectedEvent) {
        this.expectedEvent = expectedEvent;
    }

    public static Matcher<String> sameEventExceptCreatedAt(EventModel<?, ?> expectedEvent) {
        return new IsSameEvent(expectedEvent);
    }

    /**
     * Subclasses should implement this. The item will already have been checked for
     * the specific type and will never be null.
     *
     * @param itemAsJson
     */
    @Override
    protected boolean matchesSafely(String itemAsJson) {
        if (expectedEvent == null) {
            return false;
        }
        LOGGER.trace("convert the following json to map: {}", itemAsJson);
        Map<?, ?> eventMap = convertJsonStringToMap(itemAsJson);
        LOGGER.info("Event Map: {} ", eventMap);
        eventMap.remove("eventCreatedAt");
        LOGGER.info("Event Map: {} ", eventMap);
        Map<?, ?> mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent);
        LOGGER.trace("Got the map: {}", eventMap);
        LOGGER.trace("Compare to the expected map: {}", mapExpectedEvent);
        return eventMap.equals(mapExpectedEvent);
    }

    /**
     * Generates a description of the object.  The description may be part of a
     * a description of a larger object of which this is just a component, so it
     * should be worded appropriately.
     *
     * @param description The description to be built or appended to.
     */
    @Override
    public void describeTo(Description description) {
        String expectedJson = convertObjectToJsonString(expectedEvent);
        description.appendText("expected to look like " + expectedJson);

    }

    private Map<?, ?> convertJsonStringToMap(String eventAsJson) {
        try {
            return objectMapper.readValue(eventAsJson, new TypeReference<HashMap<?, ?>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<?, ?> convertObjectToMap(Object object) {
        JsonNode jsonNode = objectMapper.convertValue(object, JsonNode.class);
        return objectMapper.convertValue(jsonNode, Map.class);
    }

    private String convertObjectToJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<?, ?> getMapWithoutCreatedAt(EventModel<?, ?> eventModel) {
        Map<?, ?> map = convertObjectToMap(eventModel);
        map.remove("eventCreatedAt");
        return map;
    }
}
