package com.example.demo.controller;

import com.example.demo.service.TriviaService;
import com.example.demo.service.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class TriviaController {

    private final TriviaService triviaService;

    public TriviaController(TriviaService triviaService) {
        this.triviaService = triviaService;
    }

    @GetMapping("/questions")
    public QuizResponse getQuestions(@RequestParam(defaultValue = "10") int amount) {
        int safeAmount = Math.max(1, Math.min(amount, 50));
        return triviaService.getQuiz(safeAmount);
    }

    @PostMapping("/checkanswers")
    public CheckAnswersBatchResponse checkAnswers(@Valid @RequestBody CheckAnswersBatchRequest request) {
        return triviaService.checkAnswers(request);
    }
}