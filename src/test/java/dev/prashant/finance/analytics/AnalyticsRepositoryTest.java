package dev.prashant.finance.analytics;

import dev.prashant.finance.transaction.FinancialTransaction;
import dev.prashant.finance.transaction.TransactionCategory;
import dev.prashant.finance.transaction.TransactionDirection;
import dev.prashant.finance.transaction.TransactionFilter;
import dev.prashant.finance.transaction.TransactionRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sqlite.SQLiteDataSource;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsRepositoryTest {

    @TempDir
    Path temporaryDirectory;

    private TransactionRepository transactionRepository;
    private AnalyticsRepository analyticsRepository;

    @BeforeEach
    void setUp() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + temporaryDirectory.resolve("test.db"));
        Flyway.configure().dataSource(dataSource).load().migrate();

        JdbcClient jdbcClient = JdbcClient.create(dataSource);
        transactionRepository = new TransactionRepository(jdbcClient);
        analyticsRepository = new AnalyticsRepository(jdbcClient);

        insert("T1", "2026-10-04", "Fresh Market", 10_320, TransactionCategory.GROCERIES);
        insert("T2", "2026-10-17", "Fresh Market", 9_785, TransactionCategory.GROCERIES);
        insert("T3", "2026-10-20", "Corner Cafe", 6_120, TransactionCategory.DINING);
        transactionRepository.insert(new FinancialTransaction(
                0, "T4", LocalDate.parse("2026-10-01"), "Acme Payroll", "Salary",
                520_000, TransactionDirection.CREDIT, TransactionCategory.INCOME, "USD"));
    }

    @Test
    void calculatesDebitSpendingWithoutIncludingIncome() {
        long total = analyticsRepository.totalSpending(
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"), null);

        assertThat(total).isEqualTo(26_225);
    }

    @Test
    void filtersTransactionsAndCalculatesCategoryTotals() {
        var page = transactionRepository.find(new TransactionFilter(
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"),
                TransactionCategory.GROCERIES, null, 0, 20));

        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).extracting(FinancialTransaction::merchant)
                .containsOnly("Fresh Market");
        assertThat(analyticsRepository.totalSpending(
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"),
                TransactionCategory.GROCERIES)).isEqualTo(20_105);
    }

    @Test
    void ranksMerchantsByExactTotals() {
        var merchants = analyticsRepository.topMerchants(
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"), 2);

        assertThat(merchants).containsExactly(
                new AnalyticsRepository.MerchantTotal("Fresh Market", 20_105),
                new AnalyticsRepository.MerchantTotal("Corner Cafe", 6_120));
    }

    private void insert(String id, String date, String merchant, long cents, TransactionCategory category) {
        transactionRepository.insert(new FinancialTransaction(
                0, id, LocalDate.parse(date), merchant, "Test transaction", cents,
                TransactionDirection.DEBIT, category, "USD"));
    }
}

