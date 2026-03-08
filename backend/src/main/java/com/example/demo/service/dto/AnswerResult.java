package com.example.demo.service.dto;

public record AnswerResult(
        String questionId,
        boolean correct,
        String correctAnswer
) {}
