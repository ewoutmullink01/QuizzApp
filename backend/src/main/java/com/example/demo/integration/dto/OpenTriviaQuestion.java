package com.example.demo.integration.dto;

import java.util.List;

public record OpenTriviaQuestion(
        String category,
        String type,
        String difficulty,
        String question,
        String correct_answer,
        List<String> incorrect_answers
) {}