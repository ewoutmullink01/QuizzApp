package com.example.demo.integration.dto;

import java.util.List;

public record OpenTriviaApiResponse(
        int response_code,
        List<OpenTriviaQuestion> results
) {}