package com.vs.patchmanagement.apigateway.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {
    private final ObjectMapper objectMapper;

    public JsonUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public String toJson(Object object) throws JsonProcessingException {
      return objectMapper.writeValueAsString(object);
    }
    public <Target> Target toObject(String json, Class<Target> type) throws JsonProcessingException {
        return objectMapper.readValue(json, type);
    }
}
