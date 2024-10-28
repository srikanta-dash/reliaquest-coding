package com.reliaquest.api.Exception;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class ExceptionHelper {

    public RestClientException exceptionByStatus(ResponseEntity<JsonNode> responseEntity) {
        byte[] bodyBytes = (responseEntity.hasBody()) ?
                Objects.requireNonNull(responseEntity.getBody()).toString().getBytes() : null;

        if (responseEntity.getStatusCode().is4xxClientError()) {
            return HttpClientErrorException.create(responseEntity.getStatusCode(), "Client Error",
                    responseEntity.getHeaders(), bodyBytes, StandardCharsets.UTF_8);
        }
        else if (responseEntity.getStatusCode().is5xxServerError()) {
            return HttpServerErrorException.create(responseEntity.getStatusCode(), "Server Side Error",
                    responseEntity.getHeaders(), bodyBytes, StandardCharsets.UTF_8);
        } else {
            return new RestClientException("Error from server side. status code: " + responseEntity.getStatusCode() +
                    "response body: " + responseEntity.getBody());
        }
    }

}
