package io.github.eventify;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import static io.github.jframe.util.constants.Constants.DateTime.DEFAULT_TIMEZONE;

/**
 * The main class to start the Spring Boot application.
 */
@EnableCaching
@EnableScheduling
@SpringBootApplication(scanBasePackageClasses = Main.class)
public class Main extends SpringBootServletInitializer {

    /**
     * The global serial version for the application.
     */
    public static final long SERIAL_VERSION_UID = 0L;

    /**
     * The main method to run the spring boot application.
     */
    public static void main(final String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        SpringApplication.run(Main.class, args);
    }
}
