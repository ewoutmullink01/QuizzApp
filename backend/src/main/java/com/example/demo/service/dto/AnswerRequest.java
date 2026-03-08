package com.example.demo.service.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerRequest(
            @NotBlank String questionId,
            @NotBlank String selectedAnswer
) {}

