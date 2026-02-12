package ru.practicum.shareit.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public abstract class BaseClient {

    protected final RestTemplate restTemplate;
    protected final String serverUrl;

    protected BaseClient(RestTemplate restTemplate, String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    protected <T> ResponseEntity<T> exchange(
            HttpMethod method,
            String path,
            @Nullable Object body,
            HttpHeaders headers,
            Class<T> responseType,
            Map<String, ?> uriVars
    ) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
        String url = serverUrl + path;

        if (uriVars == null || uriVars.isEmpty()) {
            return restTemplate.exchange(url, method, requestEntity, responseType);
        }
        return restTemplate.exchange(url, method, requestEntity, responseType, uriVars);
    }

    protected HttpHeaders emptyHeaders() {
        return new HttpHeaders();
    }
}
