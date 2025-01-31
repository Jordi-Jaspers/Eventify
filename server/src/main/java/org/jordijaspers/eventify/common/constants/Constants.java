package org.jordijaspers.eventify.common.constants;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * A utility class containing constants used throughout the application.
 */
public final class Constants {

    public static final class ServerEvents {

        public static final String INITIALIZED = "INITIALIZED";
        public static final String UPDATED = "UPDATED";
    }


    /**
     * Constants used in percentages.
     */
    public static final class Percentages {

        public static final double ZERO_PERCENT = 0.00;
        public static final double FIFTY_PERCENT = 50.00;
        public static final double ONE_HUNDRED_PERCENT = 100.00;
    }


    /**
     * Constants used in time.
     */
    public static final class Time {

        public static final String DAYS = "DAYS";
        public static final String HOURS = "HOURS";
        public static final String MINUTES = "MINUTES";
        public static final String SECONDS = "SECONDS";
        public static final String MILLIS = "MILLIS";

        public static final int DAYS_PER_MONTH = 30;
        public static final int HOURS_PER_DAY = 24;
        public static final int DAYS_PER_WEEK = 7;
        public static final int HOURS_A_DAY = 24;
        public static final int MINUTES_PER_HOUR = 60;
        public static final int SECONDS_PER_MINUTE = 60;
        public static final int MILLIS_PER_SECOND = 1000;

        public static final int MILLION = 1_000_000;
        public static final int THOUSAND = 1_000;
        public static final int HUNDRED = 100;
    }


    /**
     * Constants used in date time.
     */
    public static final class DateTime {

        public static final String DEFAULT_TIMEZONE = "UTC";
        public static final ZoneId EUROPE_AMSTERDAM = ZoneId.of("Europe/Amsterdam");

        public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        public static final LocalDateTime START_OF_EPOCH = LocalDateTime.parse("1970-01-01T00:00:00");
    }


    /**
     * Constants used in email validation.
     */
    public static final class Email {

        public static final String OWASP_EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
    }


    /**
     * Constants used in password validation.
     */
    public static final class Encoder {

        public static final String BCRYPT = "bcrypt";
        public static final String SCRYPT = "scrypt";
        public static final String PBKDF2 = "pbkdf2";
    }
}
