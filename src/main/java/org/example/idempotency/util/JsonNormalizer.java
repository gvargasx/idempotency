package org.example.idempotency.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Map;

public final class JsonNormalizer {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    private JsonNormalizer() {}

    public static String normalize(String json) {
        if (json == null || json.isBlank()) return "";

        try {
            Map<String, Object> tree = mapper.readValue(
                    json,
                    new TypeReference<>() {
                    }
            );
            return mapper.writeValueAsString(tree);
        } catch (JsonProcessingException e) {
            return json.trim();
        }
    }
}
