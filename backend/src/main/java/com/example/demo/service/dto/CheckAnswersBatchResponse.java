package com.example.demo.service.dto;

import java.util.List;

public record CheckAnswersBatchResponse(
        String quizId,
        int score,
        int total,
        List<AnswerResult> results
) {}