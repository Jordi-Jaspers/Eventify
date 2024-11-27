package org.jordijaspers.eventify;

import org.hawaiiframework.boot.autoconfigure.logging.HawaiiLoggingAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.util.TimeZone;

import static org.jordijaspers.eventify.common.constants.Constants.DateTime.DEFAULT_TIMEZONE;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication(
    scanBasePackageClasses = {
        Application.class,
        HawaiiLoggingAutoConfiguration.class
    }
)
@EnableAspectJAutoProxy
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class Application extends SpringBootServletInitializer {

    /**
     * The global serial version for the application.
     */
    public static final long SERIAL_VERSION_UID = 0L;

    /**
     * The main method to run the spring boot application.
     */
    public static void main(final String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        SpringApplication.run(Application.class, args);
    }
}
