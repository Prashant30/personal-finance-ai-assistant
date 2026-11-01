package dev.prashant.finance.transaction;

import dev.prashant.finance.common.PageResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TransactionRepository {

    private static final RowMapper<FinancialTransaction> ROW_MAPPER = TransactionRepository::mapRow;
    private final JdbcClient jdbcClient;

    public TransactionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public PageResponse<FinancialTransaction> find(TransactionFilter filter) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        Map<String, Object> parameters = new HashMap<>();

        if (filter.startDate() != null) {
            where.append(" AND posted_date >= :startDate");
            parameters.put("startDate", filter.startDate().toString());
        }
        if (filter.endDate() != null) {
            where.append(" AND posted_date <= :endDate");
            parameters.put("endDate", filter.endDate().toString());
        }
        if (filter.category() != null) {
            where.append(" AND category = :category");
            parameters.put("category", filter.category().name());
        }
        if (filter.merchant() != null && !filter.merchant().isBlank()) {
            where.append(" AND lower(merchant) LIKE :merchant");
            parameters.put("merchant", "%" + filter.merchant().trim().toLowerCase() + "%");
        }

        long total = jdbcClient.sql("SELECT count(*) FROM transactions" + where)
                .params(parameters)
                .query(Long.class)
                .single();

        parameters.put("limit", filter.size());
        parameters.put("offset", filter.page() * filter.size());
        List<FinancialTransaction> transactions = jdbcClient.sql("""
                        SELECT id, external_id, posted_date, merchant, description,
                               amount_cents, direction, category, currency
                        FROM transactions
                        """ + where + " ORDER BY posted_date DESC, id DESC LIMIT :limit OFFSET :offset")
                .params(parameters)
                .query(ROW_MAPPER)
                .list();

        return PageResponse.of(transactions, filter.page(), filter.size(), total);
    }

    public long count() {
        return jdbcClient.sql("SELECT count(*) FROM transactions").query(Long.class).single();
    }

    public void insert(FinancialTransaction transaction) {
        jdbcClient.sql("""
                        INSERT OR IGNORE INTO transactions
                            (external_id, posted_date, merchant, description, amount_cents,
                             direction, category, currency)
                        VALUES
                            (:externalId, :postedDate, :merchant, :description, :amountCents,
                             :direction, :category, :currency)
                        """)
                .param("externalId", transaction.externalId())
                .param("postedDate", transaction.postedDate().toString())
                .param("merchant", transaction.merchant())
                .param("description", transaction.description())
                .param("amountCents", transaction.amountCents())
                .param("direction", transaction.direction().name())
                .param("category", transaction.category().name())
                .param("currency", transaction.currency())
                .update();
    }

    public DateRange dateRange() {
        return jdbcClient.sql("SELECT min(posted_date), max(posted_date) FROM transactions")
                .query((rs, rowNum) -> new DateRange(
                        LocalDate.parse(rs.getString(1)),
                        LocalDate.parse(rs.getString(2))))
                .single();
    }

    private static FinancialTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new FinancialTransaction(
                rs.getLong("id"),
                rs.getString("external_id"),
                LocalDate.parse(rs.getString("posted_date")),
                rs.getString("merchant"),
                rs.getString("description"),
                rs.getLong("amount_cents"),
                TransactionDirection.valueOf(rs.getString("direction")),
                TransactionCategory.valueOf(rs.getString("category")),
                rs.getString("currency"));
    }

    public record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}

