package com.reliaquest.api.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.reliaquest.api.model.Employee;
import org.springframework.http.ResponseEntity;

public interface IApiService {
    ResponseEntity<JsonNode> get(String endPoint);
    ResponseEntity<JsonNode> post(String endPoint, Object employee);
    ResponseEntity<JsonNode> delete(String endPoint, String body);
}
