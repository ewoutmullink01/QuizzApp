package com.example.demo.service;

import com.example.demo.integration.OpenTriviaClient;
import com.example.demo.integration.dto.OpenTriviaQuestion;
import com.example.demo.service.dto.QuestionResponse;
import com.example.demo.util.HtmlDecodeUtil;
import org.springframework.stereotype.Service;
import com.example.demo.service.dto.*;

import java.util.*;

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
            return new QuizResponse(UUID.randomUUID().toString(), List.of());
        }

        String quizId = UUID.randomUUID().toString();

        Map<String, String> questionToCorrect = new HashMap<>();
        List<QuestionResponse> questions = new ArrayList<>();

        for (OpenTriviaQuestion q : apiResponse.results()) {
            String questionId = UUID.randomUUID().toString();

            String correct = HtmlDecodeUtil.decode(q.correct_answer());
            questionToCorrect.put(questionId, correct);

            List<String> options = new ArrayList<>();
            options.add(correct);

            if (q.incorrect_answers() != null) {
                for (String ia : q.incorrect_answers()) {
                    options.add(HtmlDecodeUtil.decode(ia));
                }
            }
            Collections.shuffle(options);

            questions.add(new QuestionResponse(
                    questionId,
                    HtmlDecodeUtil.decode(q.category()),
                    HtmlDecodeUtil.decode(q.difficulty()),
                    HtmlDecodeUtil.decode(q.type()),
                    HtmlDecodeUtil.decode(q.question()),
                    options
            ));
        }

        cache.putQuiz(quizId, questionToCorrect);
        return new QuizResponse(quizId, questions);
    }

    public CheckAnswersBatchResponse checkAnswers(CheckAnswersBatchRequest req) {
        if (!cache.quizExists(req.quizId())) {
            List<AnswerResult> results = req.answers().stream()
                    .map(a -> new AnswerResult(a.questionId(), false, null))
                    .toList();

            return new CheckAnswersBatchResponse(req.quizId(), 0, results.size(), results);
        }

        List<AnswerResult> results = req.answers().stream()
                .map(a -> {
                    String correctAnswer = cache.getCorrectAnswer(req.quizId(), a.questionId())
                            .orElse(null);

                    boolean correct = correctAnswer != null && correctAnswer.equals(a.selectedAnswer());

                    return new AnswerResult(a.questionId(), correct, correctAnswer);
                })
                .toList();

        int score = (int) results.stream().filter(AnswerResult::correct).count();

        cache.removeQuiz(req.quizId());

        return new CheckAnswersBatchResponse(req.quizId(), score, results.size(), results);
    }
}
