package com.example.demo.service.dto;

import java.util.List;

public record QuestionResponse(
        String questionId,
        String category,
        String difficulty,
        String type,
        String question,
        List<String> options
) {}
