package dev.prashant.finance.assistant;

public record Evidence(
        String metric,
        String label,
        String value,
        String currency,
        String period
) {
}

