package dev.prashant.finance.assistant;

public record SpendingIntent(
        IntentType type,
        String startDate,
        String endDate,
        String category,
        String monthA,
        String monthB,
        int limit
) {
}

