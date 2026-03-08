package com.example.demo.integration;

import com.example.demo.integration.dto.OpenTriviaApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenTriviaClient {

    private final RestClient restClient;
    private final String baseUrl;

    public OpenTriviaClient(RestClient restClient,
                            @Value("${opentrivia.base-url:https://opentdb.com}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public OpenTriviaApiResponse fetchQuestions(int amount) {
        return restClient.get()
                .uri(baseUrl + "/api.php?amount=" + amount)
                .retrieve()
                .body(OpenTriviaApiResponse.class);
    }
}
