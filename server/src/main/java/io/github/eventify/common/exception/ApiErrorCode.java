package io.github.eventify.common.exception;


import io.github.jframe.exception.ApiError;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines an error code and reason for any exception handling.
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "ApiErrorCode")
@SuppressWarnings("PMD.ExcessivePublicCount")
public enum ApiErrorCode implements ApiError {

    INTERNAL_SERVER_ERROR(
        "ERR-0001",
        "Uncaught Exception: You think I know what went wrong here? If I did, I would've caught this exception no?"
    ),
    DATABASE_ERROR(
        "ERR-0002",
        "Could not perform the requested action on the database. Contact administrator."
    ),
    APPLICATION_NOT_FOUND(
        "ERR-0003",
        "The application with the given global application id could not be found."
    ),
    ALARM_NOT_FOUND(
        "ERR-0004",
        "The alarm with the given id could not be found."
    ),
    INDEX_PATTERN_NOT_FOUND(
        "ERR-0005",
        "The index pattern with the given id could not be found."
    ),
    QUERY_INVALID_JSON(
        "ERR-0006",
        "The Elasticsearch query should be a valid JSON."
    ),
    CANNOT_RETRIEVE_RESOURCE(
        "ERR-0007",
        "Something went wrong while retrieving the resource."
    ),
    INVALID_TOKEN_ERROR(
        "ERR-0008",
        "The provided JWT token is invalid."
    ),
    TOKEN_NOT_FOUND_ERROR(
        "ERR-0009",
        "Provided token does not exist, double check the token."
    ),
    PASSWORD_DOES_NOT_MATCH(
        "ERR-0010",
        "The provided password does not match the current password."
    ),
    USER_LOCKED_ERROR(
        "ERR-0011",
        "Authorization failed: your account has been locked by the admin."
    ),
    INVALID_CREDENTIALS(
        "ERR-0012",
        "Invalid Credentials: Username or password is incorrect."
    ),
    USER_NOT_FOUND_ERROR(
        "ERR-0013",
        "Could not find requested user."
    ),
    NO_SECURITY_CONTEXT_ERROR(
        "ERR-0014",
        "No security context found. There is no user logged in."
    ),
    SQUAD_NOT_FOUND_ERROR(
        "ERR-0015",
        "The requested squad does not exist."
    ),
    SQUAD_ALREADY_EXISTS_ERROR(
        "ERR-0016",
        "A squad with that name already exists."
    ),
    CANNOT_ACCESS_APPLICATION(
        "ERR-0017",
        "You do not have access to the requested application."
    ),
    UNAUTHORIZED_ERROR(
        "ERR-0018",
        "You are not authorized to perform this action."
    ),
    ELASTICSEARCH_SSL_ERROR(
        "ERR-0019",
        "Cannot configure SSL for Elasticsearch. Please check your configuration."
    ),
    ELASTICSEARCH_CONNECTION_ERROR(
        "ERR-0020",
        "Cannot connect to Elasticsearch. Please check your configuration."
    ),
    DATA_CONSTRAINT_ERROR(
        "ERR-0021",
        "The request you made violates a data constraint. Please check your request."
    ),
    USER_ALREADY_EXISTS_ERROR(
        "ERR-0022",
        "User cannot be created: The provided email address is already in use."
    ),
    OAUTH2_AUTHENTICATION_ERROR(
        "ERR-0023",
        "OAuth2 authentication failed. Please try again or contact support."
    ),
    NON_EXISTING_USER_ERROR(
        "ERR-0024",
        "In order to perform this action, the user must exist within the system."
    ),
    ORGANIZATION_NOT_FOUND_ERROR(
        "ERR-0025",
        "The requested organization does not exist."
    ),
    MEMBERSHIP_NOT_FOUND_ERROR(
        "ERR-0026",
        "The requested organization membership does not exist."
    ),
    USER_ALREADY_MEMBER_ERROR(
        "ERR-0027",
        "User is already a member of this organization."
    ),
    CANNOT_ADD_DISABLED_USER_ERROR(
        "ERR-0028",
        "Cannot add disabled user to organization."
    ),
    CANNOT_SET_OWNER_ROLE_ERROR(
        "ERR-0029",
        "Only global administrators can assign the OWNER role."
    ),
    CANNOT_CHANGE_OWNER_ROLE_ERROR(
        "ERR-0030",
        "Cannot change the role of the organization owner."
    ),
    CANNOT_REMOVE_OWNER_ERROR(
        "ERR-0031",
        "Cannot remove the organization owner."
    ),
    CANNOT_TRANSFER_TO_SELF_ERROR(
        "ERR-0032",
        "Cannot transfer ownership to yourself."
    ),
    NOT_ORGANIZATION_OWNER_ERROR(
        "ERR-0033",
        "Only the current owner can transfer ownership."
    ),
    ORGANIZATION_ALREADY_HAS_OWNER_ERROR(
        "ERR-0034",
        "This organization already has an owner."
    ),
    NOT_MEMBER_OF_ORGANIZATION_ERROR(
        "ERR-0035",
        "User is not a member of the specified organization."
    ),
    CANNOT_UPDATE_ROLE_AS_ADMIN_ERROR(
        "ERR-0036",
        "As an administrator, you can only update roles to MEMBER."
    ),
    CANNOT_DEMOTE_LAST_ADMIN_ERROR(
        "ERR-0037",
        "Cannot demote the last system administrator. At least one admin must exist."
    ),
    CANNOT_LOCK_SELF_ERROR(
        "ERR-0038",
        "You cannot lock your own user account."
    ),
    API_KEY_LIMIT_EXCEEDED(
        "ERR-0039",
        "Maximum number of API keys reached. Please revoke an existing key first."
    ),
    API_KEY_NOT_FOUND(
        "ERR-0040",
        "API key not found."
    ),
    API_KEY_INVALID_EXPIRATION(
        "ERR-0041",
        "Expiration date must be in the future."
    ),
    INVALID_API_KEY(
        "ERR-0042",
        "Invalid or revoked API key."
    ),
    API_KEY_EXPIRED(
        "ERR-0043",
        "API key has expired."
    ),
    API_KEY_USER_DISABLED(
        "ERR-0044",
        "User account is disabled."
    ),
    QUOTA_EXCEEDED(
        "ERR-0045",
        "Monthly event quota exceeded. Quota resets on the first of each month."
    ),
    DUPLICATE_CHANNEL_NAME(
        "ERR-0046",
        "Duplicate channel name: A channel with this name already exists for this user."
    ),
    CHANNEL_NOT_FOUND(
        "ERR-0047",
        "The requested channel has been deleted or does not exist."
    ),
    CHANNEL_PAUSED(
        "ERR-0048",
        "Your channel is paused. Resume the channel to continue sending events."
    );

    /* The error code for this reason. */
    private final String errorCode;

    /* The reason why this error occurred. */
    private final String reason;

}
