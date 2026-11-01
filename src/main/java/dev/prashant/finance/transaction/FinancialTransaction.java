package dev.prashant.finance.transaction;

import java.time.LocalDate;

public record FinancialTransaction(
        long id,
        String externalId,
        LocalDate postedDate,
        String merchant,
        String description,
        long amountCents,
        TransactionDirection direction,
        TransactionCategory category,
        String currency
) {
}

