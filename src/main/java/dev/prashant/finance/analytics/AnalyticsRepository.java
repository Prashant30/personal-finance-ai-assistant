package dev.prashant.finance.analytics;

import dev.prashant.finance.transaction.TransactionCategory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AnalyticsRepository {

    private final JdbcClient jdbcClient;

    public AnalyticsRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public long totalSpending(LocalDate startDate, LocalDate endDate, TransactionCategory category) {
        String categoryClause = category == null ? "" : " AND category = :category";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", startDate.toString());
        parameters.put("endDate", endDate.toString());
        if (category != null) {
            parameters.put("category", category.name());
        }
        return jdbcClient.sql("""
                        SELECT coalesce(sum(amount_cents), 0)
                        FROM transactions
                        WHERE direction = 'DEBIT'
                          AND posted_date BETWEEN :startDate AND :endDate
                        """ + categoryClause)
                .params(parameters)
                .query(Long.class)
                .single();
    }

    public List<CategoryTotal> categoryBreakdown(LocalDate startDate, LocalDate endDate) {
        return jdbcClient.sql("""
                        SELECT category, sum(amount_cents) AS total_cents
                        FROM transactions
                        WHERE direction = 'DEBIT'
                          AND posted_date BETWEEN :startDate AND :endDate
                        GROUP BY category
                        ORDER BY total_cents DESC, category
                        """)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .query((rs, rowNum) -> new CategoryTotal(
                        TransactionCategory.valueOf(rs.getString("category")),
                        rs.getLong("total_cents")))
                .list();
    }

    public List<MerchantTotal> topMerchants(LocalDate startDate, LocalDate endDate, int limit) {
        return jdbcClient.sql("""
                        SELECT merchant, sum(amount_cents) AS total_cents
                        FROM transactions
                        WHERE direction = 'DEBIT'
                          AND posted_date BETWEEN :startDate AND :endDate
                        GROUP BY merchant
                        ORDER BY total_cents DESC, merchant
                        LIMIT :limit
                        """)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("limit", limit)
                .query((rs, rowNum) -> new MerchantTotal(
                        rs.getString("merchant"), rs.getLong("total_cents")))
                .list();
    }

    public record CategoryTotal(TransactionCategory category, long totalCents) {
    }

    public record MerchantTotal(String merchant, long totalCents) {
    }
}

