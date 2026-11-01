package dev.prashant.finance.transaction;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sqlite.SQLiteDataSource;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DemoDataLoaderTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void loadsSyntheticDataOnlyOnce() throws Exception {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + temporaryDirectory.resolve("seed-test.db"));
        Flyway.configure().dataSource(dataSource).load().migrate();
        TransactionRepository repository = new TransactionRepository(JdbcClient.create(dataSource));
        DemoDataLoader loader = new DemoDataLoader(repository);

        loader.run(null);
        long firstCount = repository.count();
        loader.run(null);

        assertThat(firstCount).isEqualTo(73);
        assertThat(repository.count()).isEqualTo(73);
    }
}

