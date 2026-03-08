package com.example.demo.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TriviaQuestionCache {

    private final Map<String, QuizSession> sessionsByQuizId = new ConcurrentHashMap<>();

    public void putSession(QuizSession session) {
        sessionsByQuizId.put(session.quizId(), session);
    }

    public Optional<QuizSession> getSession(String quizId) {
        return Optional.ofNullable(sessionsByQuizId.get(quizId));
    }

    public Optional<String> getCorrectAnswer(String quizId, String questionId) {
        return getSession(quizId)
                .map(session -> session.correctAnswerFor(questionId));
    }

    public void removeQuiz(String quizId) {
        sessionsByQuizId.remove(quizId);
    }

    public boolean quizExists(String quizId) {
        return sessionsByQuizId.containsKey(quizId);    }
}
