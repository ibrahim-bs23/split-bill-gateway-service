package com.brainstation.ib.gateway.util;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.time.*;

@Slf4j
public class JacksonUtil {
    private static ObjectMapper objectMapper;

    public static ObjectMapper objectMapper() {
        if (objectMapper != null) {
            return objectMapper;
        }
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JavaTimeModule module = new JavaTimeModule();
        module.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        module.addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE);
        module.addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
        module.addDeserializer(ZonedDateTime.class, InstantDeserializer.ZONED_DATE_TIME);
        module.addDeserializer(OffsetDateTime.class, InstantDeserializer.OFFSET_DATE_TIME);

        objectMapper.registerModule(module);
        return objectMapper;
    }

    public static <T> T jsonToInstance(String data, Class<T> clazz) {
        try {
            return objectMapper().readValue(data, clazz);
        } catch (Exception ex) {
            log.warn("JacksonUtil.jsonToInstance: {}", ex.getMessage());
        }
        return null;
    }

    public static String objectToJson(Object object) {
        try {
            return objectMapper().writeValueAsString(object);
        } catch (Exception ex) {
            log.warn("JacksonUtil.objectToJson: {}", ex.getMessage());
        }
        return null;
    }

}
