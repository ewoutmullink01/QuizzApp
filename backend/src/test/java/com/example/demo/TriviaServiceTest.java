package com.example.demo;

import com.example.demo.integration.OpenTriviaClient;
import com.example.demo.integration.dto.OpenTriviaApiResponse;
import com.example.demo.integration.dto.OpenTriviaQuestion;
import com.example.demo.service.TriviaQuestionCache;
import com.example.demo.service.TriviaService;
import com.example.demo.service.dto.AnswerRequest;
import com.example.demo.service.dto.AnswerResult;
import com.example.demo.service.dto.CheckAnswersBatchRequest;
import com.example.demo.service.dto.CheckAnswersBatchResponse;
import com.example.demo.service.dto.QuestionResponse;
import com.example.demo.service.dto.QuizResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TriviaServiceTest {

    private OpenTriviaClient triviaClient;
    private TriviaQuestionCache cache;
    private TriviaService service;

    @BeforeEach
    void setUp() {
        triviaClient = mock(OpenTriviaClient.class);
        cache = new TriviaQuestionCache();
        service = new TriviaService(triviaClient, cache);
    }

    @Test
    void shouldBuildQuizAndCacheCorrectAnswersWhenApiReturnsOneQuestion() {
        when(triviaClient.fetchQuestions(1)).thenReturn(new OpenTriviaApiResponse(0, List.of(singleApiQuestion())));

        QuizResponse response = service.getQuiz(1);

        assertNotNull(response.quizId());
        assertEquals(1, response.questions().size());

        QuestionResponse question = response.questions().getFirst();
        assertEquals("Science & Nature", question.category());
        assertEquals("Who wrote \"1984\"?", question.question());
        assertEquals(4, question.options().size());
        assertTrue(question.options().contains("George Orwell"));
        assertTrue(cache.quizExists(response.quizId()));
    }

    @Test
    void shouldReturnQuizWithoutQuestionsWhenApiReturnsEmptyResult() {
        when(triviaClient.fetchQuestions(10)).thenReturn(new OpenTriviaApiResponse(0, List.of()));

        QuizResponse response = service.getQuiz(10);

        assertNotNull(response.quizId());
        assertTrue(response.questions().isEmpty());
        assertFalse(cache.quizExists(response.quizId()));
    }

    @Test
    void shouldScoreAnswerAndClearCacheWhenQuizIsSubmitted() {
        when(triviaClient.fetchQuestions(1)).thenReturn(new OpenTriviaApiResponse(0, List.of(historyApiQuestion())));

        QuizResponse quiz = service.getQuiz(1);
        QuestionResponse question = quiz.questions().getFirst();

        CheckAnswersBatchRequest request = new CheckAnswersBatchRequest(
                quiz.quizId(),
                List.of(new AnswerRequest(question.questionId(), "1912"))
        );

        CheckAnswersBatchResponse response = service.checkAnswers(request);

        assertEquals(1, response.score());
        assertEquals(1, response.total());
        AnswerResult result = response.results().getFirst();
        assertTrue(result.correct());
        assertEquals("1912", result.correctAnswer());
        assertFalse(cache.quizExists(quiz.quizId()));
    }

    @Test
    void shouldMarkAnswersIncorrectWhenQuizIdDoesNotExistInCache() {
        CheckAnswersBatchRequest request = new CheckAnswersBatchRequest(
                "missing-quiz",
                List.of(new AnswerRequest("q1", "answer"))
        );

        CheckAnswersBatchResponse response = service.checkAnswers(request);

        assertEquals(0, response.score());
        assertEquals(1, response.total());
        AnswerResult result = response.results().getFirst();
        assertEquals("q1", result.questionId());
        assertFalse(result.correct());
        assertNull(result.correctAnswer());
    }

    private OpenTriviaQuestion singleApiQuestion() {
        return new OpenTriviaQuestion(
                "Science &amp; Nature",
                "multiple",
                "easy",
                "Who wrote &quot;1984&quot;?",
                "George Orwell",
                List.of("Aldous Huxley", "Ray Bradbury", "H. G. Wells")
        );
    }

    private OpenTriviaQuestion historyApiQuestion() {
        return new OpenTriviaQuestion(
                "History",
                "multiple",
                "medium",
                "In which year did the Titanic sink?",
                "1912",
                List.of("1905", "1918", "1925")
        );
    }
}