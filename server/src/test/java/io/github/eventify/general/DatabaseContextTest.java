package io.github.eventify.general;

import io.github.eventify.support.IntegrationTest;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.support.container.TimescaleContainer.DATABASE_NAME;
import static io.github.jframe.util.constants.Constants.DateTime.DEFAULT_TIMEZONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Integration Test - Database Setup")
public class DatabaseContextTest extends IntegrationTest {

    @Test
    @DisplayName("Should boot up a PostgreSQL container successfully")
    public void testDatabaseContainer() {
        assertThat(postgreSQLContainer, is(notNullValue()));
        assertThat(postgreSQLContainer.isRunning(), is(true));
    }

    @Test
    @DisplayName("Database should be reachable and respond to queries")
    public void testDatabaseConnection() throws IOException, InterruptedException {
        assertThat(postgreSQLContainer.getJdbcUrl(), not(emptyString()));
        assertThat(postgreSQLContainer.execInContainer("psql", "-U", DATABASE_NAME, "-c", "SELECT 1;").getExitCode(), is(0));
    }

    @Test
    @DisplayName("Database timezone should have the same default timezone as the application")
    public void testDatabaseTimezone() throws IOException, InterruptedException {
        final String timezone = postgreSQLContainer.execInContainer("psql", "-U", DATABASE_NAME, "-c", "SHOW TIMEZONE;").getStdout();
        assertThat(timezone, containsString(DEFAULT_TIMEZONE));
    }
}
