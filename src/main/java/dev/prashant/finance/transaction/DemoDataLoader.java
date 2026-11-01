package dev.prashant.finance.transaction;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@Profile("demo")
public class DemoDataLoader implements ApplicationRunner {

    private final TransactionRepository repository;

    public DemoDataLoader(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        if (repository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource("data/synthetic-transactions.csv");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines().skip(1).filter(line -> !line.isBlank()).map(this::parse).forEach(repository::insert);
        }
    }

    private FinancialTransaction parse(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length != 8) {
            throw new IllegalArgumentException("Invalid synthetic transaction row: " + line);
        }
        return new FinancialTransaction(
                0,
                columns[0],
                java.time.LocalDate.parse(columns[1]),
                columns[2],
                columns[3],
                Long.parseLong(columns[4]),
                TransactionDirection.valueOf(columns[5]),
                TransactionCategory.valueOf(columns[6]),
                columns[7]);
    }
}

