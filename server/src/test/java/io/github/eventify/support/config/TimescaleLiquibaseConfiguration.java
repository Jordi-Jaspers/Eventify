package io.github.eventify.support.config;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Liquibase configuration that drops TimescaleDB continuous aggregates before Liquibase's dropAll.
 *
 * <p>TimescaleDB continuous aggregates require {@code DROP MATERIALIZED VIEW CASCADE} but Liquibase's
 * {@code dropAll} uses {@code DROP VIEW}, causing failures. This configuration pre-drops all continuous
 * aggregates when {@code spring.liquibase.drop-first} is enabled.</p>
 *
 * <p>Since our {@code @Primary SpringLiquibase} bean prevents Spring Boot's {@code LiquibaseAutoConfiguration}
 * from activating (it is {@code @ConditionalOnMissingBean}), we must explicitly enable
 * {@link LiquibaseProperties} via {@code @EnableConfigurationProperties}.</p>
 */
@TestConfiguration
@EnableConfigurationProperties(LiquibaseProperties.class)
public class TimescaleLiquibaseConfiguration {

    /**
     * Creates a custom {@link SpringLiquibase} bean that drops TimescaleDB continuous aggregates
     * before delegating to the standard Liquibase dropAll behavior.
     *
     * @param dataSource          the datasource for JDBC access
     * @param liquibaseProperties the Liquibase configuration properties
     * @return the configured SpringLiquibase instance
     */
    @Bean
    @Primary
    public SpringLiquibase liquibase(final DataSource dataSource, final LiquibaseProperties liquibaseProperties) {
        final SpringLiquibase liquibase = new TimescaleAwareSpringLiquibase(dataSource);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts(joinList(liquibaseProperties.getContexts()));
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        liquibase.setLabelFilter(joinList(liquibaseProperties.getLabelFilter()));
        liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setTag(liquibaseProperties.getTag());
        liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
        liquibase.setClearCheckSums(liquibaseProperties.isClearChecksums());
        return liquibase;
    }

    private static String joinList(final List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join(",", values);
    }

    /**
     * Custom {@link SpringLiquibase} subclass that drops all TimescaleDB continuous aggregates
     * before the standard Liquibase initialization runs.
     */
    private static class TimescaleAwareSpringLiquibase extends SpringLiquibase {

        private static final Logger LOGGER = LoggerFactory.getLogger(TimescaleAwareSpringLiquibase.class);

        private static final String DROP_CONTINUOUS_AGGREGATES_SQL = """
            DO $$
            DECLARE
                cagg RECORD;
            BEGIN
                FOR cagg IN
                    SELECT view_name FROM timescaledb_information.continuous_aggregates
                LOOP
                    EXECUTE format('DROP MATERIALIZED VIEW IF EXISTS %I CASCADE', cagg.view_name);
                END LOOP;
            END $$;
            """;

        private final DataSource dataSource;

        TimescaleAwareSpringLiquibase(final DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public void afterPropertiesSet() throws LiquibaseException {
            if (isDropFirst()) {
                dropContinuousAggregates();
            }
            super.afterPropertiesSet();
        }

        private void dropContinuousAggregates() {
            try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
                statement.execute(DROP_CONTINUOUS_AGGREGATES_SQL);
                LOGGER.info("Dropped TimescaleDB continuous aggregates before Liquibase dropAll");
            } catch (final Exception e) {
                LOGGER.debug(
                    "Could not drop continuous aggregates (may not exist yet): {}",
                    e.getMessage()
                );
            }
        }
    }
}
