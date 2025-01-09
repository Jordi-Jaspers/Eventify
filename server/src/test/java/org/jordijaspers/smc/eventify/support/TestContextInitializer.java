package org.jordijaspers.smc.eventify.support;


import org.jordijaspers.eventify.Application;
import org.jordijaspers.smc.eventify.support.config.BeanConfiguration;
import org.jordijaspers.smc.eventify.support.container.TimescaleContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Base class for integration tests. This class is used to import the necessary configurations for the tests.
 */
@Import(
    {
        BeanConfiguration.class,
        TimescaleContainer.class
    }
)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(
    classes = Application.class,
    webEnvironment = RANDOM_PORT
)
public class TestContextInitializer {

    // ========================= CONTAINERS =========================
    @Autowired
    @NonNull
    protected PostgreSQLContainer<?> timescaleContainer;

    // ========================= CONTEXT =========================
    @Autowired
    @NonNull
    protected WebApplicationContext applicationContext;

    @Autowired
    @NonNull
    protected HawaiiFilters hawaiiFilters;

    // ========================= APPLICATION =========================

}
