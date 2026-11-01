package dev.prashant.finance.assistant;

import dev.prashant.finance.analytics.AnalyticsRepository;
import dev.prashant.finance.transaction.TransactionCategory;
import dev.prashant.finance.transaction.TransactionRepository;
import dev.prashant.finance.transaction.TransactionRepository.DateRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AssistantService {

    private final AiGateway aiGateway;
    private final AnalyticsRepository analyticsRepository;
    private final TransactionRepository transactionRepository;
    private final String model;

    public AssistantService(
            AiGateway aiGateway,
            AnalyticsRepository analyticsRepository,
            TransactionRepository transactionRepository,
            @Value("${assistant.model}") String model) {
        this.aiGateway = aiGateway;
        this.analyticsRepository = analyticsRepository;
        this.transactionRepository = transactionRepository;
        this.model = model;
    }

    public QuestionResponse answer(String question) {
        DateRange availableDates = transactionRepository.dateRange();
        SpendingIntent intent = aiGateway.interpret(question, availableDates);
        validate(intent, availableDates);

        String requestId = UUID.randomUUID().toString();
        if (intent.type() == IntentType.UNSUPPORTED) {
            return new QuestionResponse(
                    question,
                    "This POC supports spending totals, category breakdowns, top merchants, and month comparisons.",
                    intent,
                    List.of(),
                    model,
                    requestId);
        }

        List<Evidence> evidence = execute(intent);
        String answer = evidence.isEmpty()
                ? "No matching fictional transactions were found."
                : aiGateway.explain(question, intent, evidence);
        return new QuestionResponse(question, answer, intent, evidence, model, requestId);
    }

    private List<Evidence> execute(SpendingIntent intent) {
        return switch (intent.type()) {
            case TOTAL_SPENDING -> totalEvidence(intent);
            case CATEGORY_BREAKDOWN -> breakdownEvidence(intent);
            case TOP_MERCHANTS -> merchantEvidence(intent);
            case MONTH_COMPARISON -> comparisonEvidence(intent);
            case UNSUPPORTED -> List.of();
        };
    }

    private List<Evidence> totalEvidence(SpendingIntent intent) {
        LocalDate start = LocalDate.parse(intent.startDate());
        LocalDate end = LocalDate.parse(intent.endDate());
        TransactionCategory category = blank(intent.category())
                ? null
                : TransactionCategory.valueOf(intent.category().toUpperCase(Locale.ROOT));
        long cents = analyticsRepository.totalSpending(start, end, category);
        String label = category == null ? "Total spending" : category + " spending";
        return List.of(moneyEvidence("total_spending", label, cents, start + " to " + end));
    }

    private List<Evidence> breakdownEvidence(SpendingIntent intent) {
        LocalDate start = LocalDate.parse(intent.startDate());
        LocalDate end = LocalDate.parse(intent.endDate());
        String period = start + " to " + end;
        return analyticsRepository.categoryBreakdown(start, end).stream()
                .map(total -> moneyEvidence(
                        "category_spending", total.category().name(), total.totalCents(), period))
                .toList();
    }

    private List<Evidence> merchantEvidence(SpendingIntent intent) {
        LocalDate start = LocalDate.parse(intent.startDate());
        LocalDate end = LocalDate.parse(intent.endDate());
        String period = start + " to " + end;
        return analyticsRepository.topMerchants(start, end, intent.limit()).stream()
                .map(total -> moneyEvidence(
                        "merchant_spending", total.merchant(), total.totalCents(), period))
                .toList();
    }

    private List<Evidence> comparisonEvidence(SpendingIntent intent) {
        YearMonth firstMonth = YearMonth.parse(intent.monthA());
        YearMonth secondMonth = YearMonth.parse(intent.monthB());
        long first = analyticsRepository.totalSpending(
                firstMonth.atDay(1), firstMonth.atEndOfMonth(), null);
        long second = analyticsRepository.totalSpending(
                secondMonth.atDay(1), secondMonth.atEndOfMonth(), null);

        List<Evidence> evidence = new ArrayList<>();
        evidence.add(moneyEvidence("monthly_spending", firstMonth.toString(), first, firstMonth.toString()));
        evidence.add(moneyEvidence("monthly_spending", secondMonth.toString(), second, secondMonth.toString()));
        evidence.add(moneyEvidence(
                "spending_difference",
                secondMonth + " minus " + firstMonth,
                second - first,
                firstMonth + " compared with " + secondMonth));
        return List.copyOf(evidence);
    }

    private void validate(SpendingIntent intent, DateRange availableDates) {
        if (intent == null || intent.type() == null) {
            throw new IllegalArgumentException("AI returned no supported intent");
        }
        try {
            switch (intent.type()) {
                case TOTAL_SPENDING, CATEGORY_BREAKDOWN, TOP_MERCHANTS -> {
                    LocalDate start = LocalDate.parse(intent.startDate());
                    LocalDate end = LocalDate.parse(intent.endDate());
                    if (start.isAfter(end)) {
                        throw new IllegalArgumentException("Intent startDate must not be after endDate");
                    }
                    if (start.isBefore(availableDates.startDate()) || end.isAfter(availableDates.endDate())) {
                        throw new IllegalArgumentException("Requested dates are outside the available demo data");
                    }
                    if (!blank(intent.category())) {
                        TransactionCategory.valueOf(intent.category().toUpperCase(Locale.ROOT));
                    }
                    if (intent.type() == IntentType.TOP_MERCHANTS
                            && (intent.limit() < 1 || intent.limit() > 10)) {
                        throw new IllegalArgumentException("Top merchant limit must be between 1 and 10");
                    }
                }
                case MONTH_COMPARISON -> {
                    YearMonth first = YearMonth.parse(intent.monthA());
                    YearMonth second = YearMonth.parse(intent.monthB());
                    if (first.atDay(1).isBefore(YearMonth.from(availableDates.startDate()).atDay(1))
                            || second.atEndOfMonth().isAfter(YearMonth.from(availableDates.endDate()).atEndOfMonth())) {
                        throw new IllegalArgumentException("Comparison months are outside the available demo data");
                    }
                }
                case UNSUPPORTED -> {
                    // No parameters are needed.
                }
            }
        } catch (DateTimeException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("AI returned invalid intent parameters: " + exception.getMessage(), exception);
        }
    }

    private Evidence moneyEvidence(String metric, String label, long cents, String period) {
        return new Evidence(metric, label, BigDecimal.valueOf(cents, 2).toPlainString(), "USD", period);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}

