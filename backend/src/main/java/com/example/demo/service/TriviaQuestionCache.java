package com.example.demo.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TriviaQuestionCache {

    private final Map<String, Map<String, String>> quizzes = new ConcurrentHashMap<>();

    public void putQuiz(String quizId, Map<String, String> questionToCorrectAnswer) {
        quizzes.put(quizId, new ConcurrentHashMap<>(questionToCorrectAnswer));
    }

    public Optional<String> getCorrectAnswer(String quizId, String questionId) {
        Map<String, String> qmap = quizzes.get(quizId);
        if (qmap == null) return Optional.empty();
        return Optional.ofNullable(qmap.get(questionId));
    }

    public void removeQuiz(String quizId) {
        quizzes.remove(quizId);
    }

    public boolean quizExists(String quizId) {
        return quizzes.containsKey(quizId);
    }
}
