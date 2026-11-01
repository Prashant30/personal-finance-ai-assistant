package dev.prashant.finance.transaction;

import java.time.LocalDate;

public record TransactionFilter(
        LocalDate startDate,
        LocalDate endDate,
        TransactionCategory category,
        String merchant,
        int page,
        int size
) {
    public TransactionFilter {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
    }
}

