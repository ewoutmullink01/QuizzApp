package com.example.demo.service;

import com.example.demo.service.dto.QuestionResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record QuizSession(
        String quizId,
        List<QuestionResponse> questions,
        Map<String, String> correctAnswersByQuestionId
) {
    public QuizSession {
        questions = List.copyOf(questions);
        correctAnswersByQuestionId = Map.copyOf(correctAnswersByQuestionId);
    }

    public String correctAnswerFor(String questionId) {
        return correctAnswersByQuestionId.get(questionId);
    }
}