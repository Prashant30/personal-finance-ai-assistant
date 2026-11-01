package dev.prashant.finance.assistant;

import dev.prashant.finance.transaction.TransactionRepository.DateRange;

import java.util.List;

public interface AiGateway {

    SpendingIntent interpret(String question, DateRange availableDates);

    String explain(String question, SpendingIntent intent, List<Evidence> evidence);
}

