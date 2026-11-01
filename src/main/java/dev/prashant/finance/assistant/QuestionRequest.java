package dev.prashant.finance.assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionRequest(
        @NotBlank(message = "question is required")
        @Size(max = 500, message = "question must not exceed 500 characters")
        String question
) {
}

