package com.example.demo.service;

import com.example.demo.integration.OpenTriviaClient;
import com.example.demo.integration.dto.OpenTriviaApiResponse;
import com.example.demo.integration.dto.OpenTriviaQuestion;
import com.example.demo.service.dto.AnswerRequest;
import com.example.demo.service.dto.AnswerResult;
import com.example.demo.service.dto.CheckAnswersBatchRequest;
import com.example.demo.service.dto.CheckAnswersBatchResponse;
import com.example.demo.service.dto.QuestionResponse;
import com.example.demo.service.dto.QuizResponse;
import com.example.demo.util.HtmlDecodeUtil;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TriviaService {

    private final OpenTriviaClient triviaClient;
    private final TriviaQuestionCache cache;

    public TriviaService(OpenTriviaClient triviaClient, TriviaQuestionCache cache) {
        this.triviaClient = triviaClient;
        this.cache = cache;
    }

    public QuizResponse getQuiz(int amount) {
        var apiResponse = triviaClient.fetchQuestions(amount);

        if (hasNoQuestions(apiResponse)) {
            return createEmptyQuiz();
        }

        String quizId = newQuizId();
        Map<String, String> correctAnswersByQuestionId = new HashMap<>();
        List<QuestionResponse> questions = buildQuestions(apiResponse.results(), correctAnswersByQuestionId);

        QuizSession session = createQuizSession(quizId, questions, correctAnswersByQuestionId);
        storeQuizSession(session);

        return new QuizResponse(quizId, questions);
    }

    public CheckAnswersBatchResponse checkAnswers(CheckAnswersBatchRequest request) {
        if (quizDoesNotExist(request.quizId())) {
            return createMissingQuizResponse(request);
        }

        List<AnswerResult> results = evaluateAnswers(request);
        int score = calculateScore(results);

        removeQuizFromCache(request.quizId());

        return new CheckAnswersBatchResponse(
                request.quizId(),
                score,
                results.size(),
                results
        );
    }

    private boolean hasNoQuestions(OpenTriviaApiResponse response) {
        return response == null || response.results() == null || response.results().isEmpty();
    }

    private QuizResponse createEmptyQuiz() {
        return new QuizResponse(newQuizId(), List.of());
    }

    private String newQuizId() {
        return UUID.randomUUID().toString();
    }

    private String newQuestionId() {
        return UUID.randomUUID().toString();
    }

    private List<QuestionResponse> buildQuestions(
            List<OpenTriviaQuestion> openTriviaQuestions,
            Map<String, String> correctAnswersByQuestionId
    ) {
        List<QuestionResponse> questions = new ArrayList<>();

        for (OpenTriviaQuestion openTriviaQuestion : openTriviaQuestions) {
            questions.add(buildQuestion(openTriviaQuestion, correctAnswersByQuestionId));
        }

        return questions;
    }

    private QuestionResponse buildQuestion(
            OpenTriviaQuestion openTriviaQuestion,
            Map<String, String> correctAnswersByQuestionId
    ) {
        String questionId = newQuestionId();
        String correctAnswer = decode(openTriviaQuestion.correct_answer());

        rememberCorrectAnswer(correctAnswersByQuestionId, questionId, correctAnswer);

        return new QuestionResponse(
                questionId,
                decode(openTriviaQuestion.category()),
                decode(openTriviaQuestion.difficulty()),
                decode(openTriviaQuestion.type()),
                decode(openTriviaQuestion.question()),
                buildOptions(correctAnswer, openTriviaQuestion.incorrect_answers())
        );
    }

    private void rememberCorrectAnswer(
            Map<String, String> correctAnswersByQuestionId,
            String questionId,
            String correctAnswer
    ) {
        correctAnswersByQuestionId.put(questionId, correctAnswer);
    }

    private List<String> buildOptions(String correctAnswer, List<String> incorrectAnswers) {
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);
        addIncorrectAnswers(options, incorrectAnswers);
        shuffle(options);
        return options;
    }

    private void addIncorrectAnswers(List<String> options, List<String> incorrectAnswers) {
        if (incorrectAnswers == null) {
            return;
        }

        for (String incorrectAnswer : incorrectAnswers) {
            options.add(decode(incorrectAnswer));
        }
    }

    private void shuffle(List<String> options) {
        Collections.shuffle(options);
    }

    private QuizSession createQuizSession(
            String quizId,
            List<QuestionResponse> questions,
            Map<String, String> correctAnswersByQuestionId
    ) {
        return new QuizSession(quizId, questions, correctAnswersByQuestionId);
    }

    private void storeQuizSession(QuizSession session) {
        cache.putSession(session);
    }

    private boolean quizDoesNotExist(String quizId) {
        return !cache.quizExists(quizId);
    }

    private CheckAnswersBatchResponse createMissingQuizResponse(CheckAnswersBatchRequest request) {
        List<AnswerResult> results = createIncorrectResultsWithoutCorrectAnswer(request.answers());

        return new CheckAnswersBatchResponse(
                request.quizId(),
                0,
                results.size(),
                results
        );
    }

    private List<AnswerResult> createIncorrectResultsWithoutCorrectAnswer(List<AnswerRequest> answers) {
        return answers.stream()
                .map(answer -> new AnswerResult(answer.questionId(), false, null))
                .toList();
    }

    private List<AnswerResult> evaluateAnswers(CheckAnswersBatchRequest request) {
        return request.answers().stream()
                .map(answer -> evaluateAnswer(request.quizId(), answer))
                .toList();
    }

    private AnswerResult evaluateAnswer(String quizId, AnswerRequest answer) {
        String correctAnswer = findCorrectAnswer(quizId, answer.questionId());
        boolean correct = isCorrectAnswer(correctAnswer, answer.selectedAnswer());

        return new AnswerResult(
                answer.questionId(),
                correct,
                correctAnswer
        );
    }

    private String findCorrectAnswer(String quizId, String questionId) {
        return cache.getCorrectAnswer(quizId, questionId).orElse(null);
    }

    private boolean isCorrectAnswer(String correctAnswer, String selectedAnswer) {
        return correctAnswer != null && correctAnswer.equals(selectedAnswer);
    }

    private int calculateScore(List<AnswerResult> results) {
        return (int) results.stream()
                .filter(AnswerResult::correct)
                .count();
    }

    private void removeQuizFromCache(String quizId) {
        cache.removeQuiz(quizId);
    }

    private String decode(String value) {
        return HtmlDecodeUtil.decode(value);
    }
}