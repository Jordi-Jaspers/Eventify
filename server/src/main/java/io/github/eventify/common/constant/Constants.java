package io.github.eventify.common.constant;


import lombok.experimental.UtilityClass;

/**
 * A utility class containing constants used throughout the application.
 */
@UtilityClass
public final class Constants {

    /**
     * OAuth attributes keys.
     */
    public static final class OAuthAttributes {

        public static final String PROVIDER_ID = "provider_id";
        public static final String PROVIDER = "provider";

        public static final String EMAIL = "email";
        public static final String EMAIL_VERIFIED = "email_verified";

        public static final String ID = "id";
        public static final String SUB = "sub";
        public static final String NAME = "name";
        public static final String GIVEN_NAME = "given_name";
        public static final String FAMILY_NAME = "family_name";

    }


    /**
     * Constants used for security.
     */
    public static final class Security {

        public static final String BEARER = "Bearer ";
        public static final String BASIC = "Basic";
        public static final String ACCESS_TOKEN_COOKIE = "EVENTIFY_ACCESS_TOKEN";
        public static final String REFRESH_TOKEN_COOKIE = "EVENTIFY_REFRESH_TOKEN";
    }


    /**
     * Email relates variables.
     */
    public static final class EmailVariables {

        public static final String TOKEN = "token";
        public static final String EMAIL_ADDRESS = "email_address";
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


    /**
     * Constants used in RSA key properties.
     */
    public static final class KeyGeneration {

        public static final String RSA = "RSA";
        public static final int KEY_SIZE = 2048;
    }

}
