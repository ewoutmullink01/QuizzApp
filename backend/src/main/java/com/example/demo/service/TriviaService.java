package com.example.demo.service;

import com.example.demo.integration.OpenTriviaClient;
import com.example.demo.integration.dto.OpenTriviaQuestion;
import com.example.demo.service.dto.AnswerResult;
import com.example.demo.service.dto.CheckAnswersBatchRequest;
import com.example.demo.service.dto.CheckAnswersBatchResponse;
import com.example.demo.service.dto.QuestionResponse;
import com.example.demo.service.dto.QuizResponse;
import com.example.demo.util.HtmlDecodeUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TriviaService {

    private final OpenTriviaClient openTriviaClient;
    private final TriviaQuestionCache cache;

    public TriviaService(OpenTriviaClient openTriviaClient, TriviaQuestionCache cache) {
        this.openTriviaClient = openTriviaClient;
        this.cache = cache;
    }

    public QuizResponse getQuiz(int amount) {
        var apiResponse = openTriviaClient.fetchQuestions(amount);

        if (apiResponse == null || apiResponse.results() == null || apiResponse.results().isEmpty()) {
            return emptyQuizResponse();
        }

        String quizId = UUID.randomUUID().toString();
        Map<String, String> correctAnswersByQuestionId = new HashMap<>();
        List<QuestionResponse> questions = new ArrayList<>();

        for (OpenTriviaQuestion question : apiResponse.results()) {
            QuestionResponse questionResponse = toQuestionResponse(question, correctAnswersByQuestionId);
            questions.add(questionResponse);
        }

        cache.putQuiz(quizId, correctAnswersByQuestionId);
        return new QuizResponse(quizId, questions);
    }

    public CheckAnswersBatchResponse checkAnswers(CheckAnswersBatchRequest request) {
        if (!cache.quizExists(request.quizId())) {
            return createMissingQuizResponse(request);
        }

        List<AnswerResult> results = request.answers().stream()
                .map(answer -> toAnswerResult(request.quizId(), answer.questionId(), answer.selectedAnswer()))
                .toList();

        int score = (int) results.stream()
                .filter(AnswerResult::correct)
                .count();

        cache.removeQuiz(request.quizId());

        return new CheckAnswersBatchResponse(
                request.quizId(),
                score,
                results.size(),
                results
        );
    }

    private QuizResponse emptyQuizResponse() {
        return new QuizResponse(UUID.randomUUID().toString(), List.of());
    }

    private QuestionResponse toQuestionResponse(
            OpenTriviaQuestion question,
            Map<String, String> correctAnswersByQuestionId
    ) {
        String questionId = UUID.randomUUID().toString();
        String correctAnswer = decode(question.correct_answer());

        correctAnswersByQuestionId.put(questionId, correctAnswer);

        List<String> options = buildShuffledOptions(correctAnswer, question.incorrect_answers());

        return new QuestionResponse(
                questionId,
                decode(question.category()),
                decode(question.difficulty()),
                decode(question.type()),
                decode(question.question()),
                options
        );
    }

    private List<String> buildShuffledOptions(String correctAnswer, List<String> incorrectAnswers) {
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);

        if (incorrectAnswers != null) {
            for (String incorrectAnswer : incorrectAnswers) {
                options.add(decode(incorrectAnswer));
            }
        }

        Collections.shuffle(options);
        return options;
    }

    private CheckAnswersBatchResponse createMissingQuizResponse(CheckAnswersBatchRequest request) {
        List<AnswerResult> results = request.answers().stream()
                .map(answer -> new AnswerResult(answer.questionId(), false, null))
                .toList();

        return new CheckAnswersBatchResponse(
                request.quizId(),
                0,
                results.size(),
                results
        );
    }

    private AnswerResult toAnswerResult(String quizId, String questionId, String selectedAnswer) {
        String correctAnswer = cache.getCorrectAnswer(quizId, questionId).orElse(null);
        boolean isCorrect = correctAnswer != null && correctAnswer.equals(selectedAnswer);

        return new AnswerResult(questionId, isCorrect, correctAnswer);
    }

    private String decode(String value) {
        return HtmlDecodeUtil.decode(value);
    }
}