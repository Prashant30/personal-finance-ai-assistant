package dev.prashant.finance.assistant;

import dev.prashant.finance.transaction.TransactionRepository.DateRange;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringAiGateway implements AiGateway {

    private static final String INTENT_SYSTEM_PROMPT = """
            You classify questions about fictional personal-finance transactions.
            Select exactly one supported intent:
            - TOTAL_SPENDING: total debit spending for a date range and optional category.
            - CATEGORY_BREAKDOWN: debit totals grouped by category for a date range.
            - TOP_MERCHANTS: merchants with the highest debit totals for a date range.
            - MONTH_COMPARISON: compare total debit spending in two calendar months.
            - UNSUPPORTED: anything else, including advice, predictions, income, or account actions.

            Dates use ISO-8601. Months use YYYY-MM. Categories are uppercase enum-style names.
            Valid categories: HOUSING, GROCERIES, DINING, TRANSPORTATION, UTILITIES,
            SUBSCRIPTIONS, SHOPPING, ENTERTAINMENT, HEALTHCARE, TRAVEL, OTHER.
            Use empty strings for fields that do not apply. Use limit 5 unless explicitly requested,
            and never return a limit outside 1 through 10. Do not invent dates outside the available range.
            """;

    private final ChatClient chatClient;

    public SpringAiGateway(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public SpendingIntent interpret(String question, DateRange availableDates) {
        try {
            return chatClient.prompt()
                    .system(INTENT_SYSTEM_PROMPT)
                    .user("Available transaction dates: %s through %s.%nQuestion: %s"
                            .formatted(availableDates.startDate(), availableDates.endDate(), question))
                    .call()
                    .entity(SpendingIntent.class, spec -> spec.validateSchema());
        } catch (RuntimeException exception) {
            throw new AiServiceException("The AI model could not interpret the question", exception);
        }
    }

    @Override
    public String explain(String question, SpendingIntent intent, List<Evidence> evidence) {
        try {
            return chatClient.prompt()
                    .system("""
                            Explain fictional spending results in at most three concise sentences.
                            Use only the supplied evidence. Never recalculate, invent values, give financial
                            advice, or claim that the data belongs to a real person.
                            """)
                    .user("Question: %s%nIntent: %s%nEvidence: %s"
                            .formatted(question, intent.type(), evidence))
                    .call()
                    .content();
        } catch (RuntimeException exception) {
            throw new AiServiceException("The AI model could not explain the results", exception);
        }
    }
}

