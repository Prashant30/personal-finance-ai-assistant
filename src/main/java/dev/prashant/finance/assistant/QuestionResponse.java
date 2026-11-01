package dev.prashant.finance.assistant;

import java.util.List;

public record QuestionResponse(
        String question,
        String answer,
        SpendingIntent intent,
        List<Evidence> evidence,
        String model,
        String requestId
) {
}

