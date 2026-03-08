package com.example.demo.service.dto;

import java.util.List;

public record QuizResponse(
        String quizId,
        List<QuestionResponse> questions
) {}
