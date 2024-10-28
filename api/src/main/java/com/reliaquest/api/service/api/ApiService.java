package com.reliaquest.api.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService implements IApiService{
    @Autowired
    RestTemplate restTemplate;
    @Value("${api.base.url}")
    private String apiBaseUrl;

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    @Override
    public ResponseEntity<JsonNode> get(String endPoint) {
        logger.info("GET API Call for {}", apiBaseUrl + endPoint);
        return restTemplate.getForEntity(apiBaseUrl + endPoint, JsonNode.class);
    }

    @Override
    public ResponseEntity<JsonNode> post(String endPoint, Object employee) {
        logger.info("POST API Call for {}", apiBaseUrl + endPoint);
        return restTemplate.postForEntity(apiBaseUrl + endPoint, employee, JsonNode.class);
    }

    @Override
    public ResponseEntity<JsonNode> delete(String endPoint, String id) {
        HttpEntity<String> entity = getHttpEntity(id);
        logger.info("DELETE API Call for {}", apiBaseUrl + endPoint);
        return restTemplate.exchange(apiBaseUrl + endPoint, HttpMethod.DELETE, entity, JsonNode.class);
    }

    private static HttpEntity<String> getHttpEntity(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonBody = "{\"name\":\""+ id +"\"}";
        return new HttpEntity<>(jsonBody, headers);
    }

}
