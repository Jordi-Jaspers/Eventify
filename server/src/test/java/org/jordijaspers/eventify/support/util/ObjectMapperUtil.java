package org.jordijaspers.eventify.support.util;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static java.util.Objects.isNull;

/**
 * Utility class for serializing and deserializing objects to and from JSON.
 */
@Slf4j
public final class ObjectMapperUtil {

    private static final ObjectMapper MAPPER = createObjectMapper();

    private ObjectMapperUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    public static String toJson(final Object object) {
        try {
            return isNull(object)
                ? ""
                : MAPPER.writeValueAsString(object);
        } catch (final JsonProcessingException exception) {
            log.error("Failed to serialize object to JSON: {}", object.getClass().getSimpleName(), exception);
            return "";
        }
    }

    public static <T> T fromJson(final String json, final Class<T> targetClass) {
        try {
            return MAPPER.readValue(json, targetClass);
        } catch (final JsonProcessingException e) {
            log.error("Failed to deserialize JSON for complex type", e);
            throw new AssertionError("Failed to deserialize JSON for complex type", e);
        }
    }

    public static <T> T fromJson(final String json, final TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (final JsonProcessingException e) {
            log.error("Failed to deserialize JSON for complex type", e);
            throw new AssertionError("Failed to deserialize JSON for complex type", e);
        }
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
