package dev.prashant.finance.assistant;

import dev.prashant.finance.analytics.AnalyticsRepository;
import dev.prashant.finance.transaction.TransactionCategory;
import dev.prashant.finance.transaction.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssistantServiceTest {

    private AiGateway aiGateway;
    private AnalyticsRepository analyticsRepository;
    private TransactionRepository transactionRepository;
    private AssistantService service;
    private final TransactionRepository.DateRange availableDates = new TransactionRepository.DateRange(
            LocalDate.parse("2026-05-01"), LocalDate.parse("2026-10-29"));

    @BeforeEach
    void setUp() {
        aiGateway = mock(AiGateway.class);
        analyticsRepository = mock(AnalyticsRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        when(transactionRepository.dateRange()).thenReturn(availableDates);
        service = new AssistantService(
                aiGateway, analyticsRepository, transactionRepository, "Qwen/Qwen3-8B");
    }

    @Test
    void returnsDatabaseEvidenceAndModelExplanation() {
        SpendingIntent intent = new SpendingIntent(
                IntentType.TOTAL_SPENDING,
                "2026-10-01",
                "2026-10-29",
                "GROCERIES",
                "",
                "",
                5);
        when(aiGateway.interpret(any(), any())).thenReturn(intent);
        when(analyticsRepository.totalSpending(
                LocalDate.parse("2026-10-01"),
                LocalDate.parse("2026-10-29"),
                TransactionCategory.GROCERIES)).thenReturn(20_105L);
        when(aiGateway.explain(any(), any(), any())).thenReturn(
                "Fictional grocery spending was $201.05 in October 2026.");

        QuestionResponse response = service.answer("How much was spent on groceries in October?");

        assertThat(response.answer()).contains("$201.05");
        assertThat(response.evidence()).containsExactly(new Evidence(
                "total_spending", "GROCERIES spending", "201.05", "USD",
                "2026-10-01 to 2026-10-29"));
        assertThat(response.model()).isEqualTo("Qwen/Qwen3-8B");
        assertThat(response.requestId()).isNotBlank();
    }

    @Test
    void handlesUnsupportedQuestionsWithoutSecondModelCall() {
        SpendingIntent intent = new SpendingIntent(
                IntentType.UNSUPPORTED, "", "", "", "", "", 5);
        when(aiGateway.interpret(any(), any())).thenReturn(intent);

        QuestionResponse response = service.answer("Which stock should I buy?");

        assertThat(response.answer()).contains("supports spending totals");
        assertThat(response.evidence()).isEmpty();
        verify(aiGateway, never()).explain(any(), any(), any());
    }

    @Test
    void rejectsModelDatesOutsideAvailableData() {
        SpendingIntent intent = new SpendingIntent(
                IntentType.CATEGORY_BREAKDOWN,
                "2026-01-01",
                "2026-01-31",
                "",
                "",
                "",
                5);
        when(aiGateway.interpret(any(), any())).thenReturn(intent);

        assertThatThrownBy(() -> service.answer("Show January spending"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside the available demo data");
    }

    @Test
    void comparesTwoMonthsUsingDeterministicCalculations() {
        SpendingIntent intent = new SpendingIntent(
                IntentType.MONTH_COMPARISON, "", "", "", "2026-09", "2026-10", 5);
        when(aiGateway.interpret(any(), any())).thenReturn(intent);
        when(analyticsRepository.totalSpending(
                LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-30"), null))
                .thenReturn(215_000L);
        when(analyticsRepository.totalSpending(
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"), null))
                .thenReturn(220_500L);
        when(aiGateway.explain(any(), any(), any())).thenReturn("October was $55.00 higher than September.");

        QuestionResponse response = service.answer("Compare September and October");

        assertThat(response.evidence()).contains(new Evidence(
                "spending_difference", "2026-10 minus 2026-09", "55.00", "USD",
                "2026-09 compared with 2026-10"));
    }
}

