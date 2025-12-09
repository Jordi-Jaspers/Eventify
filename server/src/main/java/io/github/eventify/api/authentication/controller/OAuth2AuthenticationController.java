package io.github.eventify.api.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.OAUTH2_AUTHORIZATION_PROVIDER_PATH;
import static io.github.eventify.api.Paths.OAUTH2_CALLBACK_PATH;

/**
 * Documentation controller for OAuth2 authentication endpoints.
 * <p>
 * Note: These endpoints are managed by Spring Security's OAuth2 client functionality.
 * This controller exists solely to provide OpenAPI/Swagger documentation for the OAuth2 flow.
 * The actual implementations are handled by Spring Security filters and handlers:
 * <ul>
 * <li>{@link io.github.eventify.common.security.oauth2.CustomOAuth2UserService} - Processes OAuth2 user information</li>
 * <li>{@link io.github.eventify.common.security.oauth2.OAuth2AuthenticationSuccessHandler} - Handles successful authentication</li>
 * <li>{@link io.github.eventify.common.security.oauth2.OAuth2AuthenticationFailureHandler} - Handles authentication failures</li>
 * </ul>
 */
@RestController
@Tag(
    name = "OAuth2 Authentication",
    description = "OAuth2 social login endpoints for Google and GitHub authentication. "
        + "These endpoints initiate the OAuth2 authorization code flow and handle provider callbacks."
)
public class OAuth2AuthenticationController {

    private static final String SPRING_SECURITY_HANDLED_MESSAGE = "This endpoint is handled by Spring Security OAuth2 client";

    /**
     * Initiates OAuth2 login flow with the specified provider.
     * <p>
     * This endpoint redirects the user to the OAuth2 provider's authorization page.
     * After successful authorization, the provider redirects back to the callback endpoint.
     * <p>
     * Supported providers: google, github
     *
     * @param provider The OAuth2 provider (google or github)
     */
    @Operation(
        summary = "Initiate OAuth2 login",
        description = """
            Initiates the OAuth2 authorization code flow with the specified provider.

            **Flow:**
            1. User clicks login button and is redirected to this endpoint
            2. Spring Security redirects to the provider's authorization page
            3. User authenticates with the provider and grants permissions
            4. Provider redirects back to the callback endpoint with an authorization code
            5. Spring Security exchanges the code for an access token
            6. User information is retrieved and processed
            7. JWT tokens are issued and set as HTTP-only cookies
            8. User is redirected to the frontend application

            **Supported Providers:**
            - `google` - Google OAuth2
            - `github` - GitHub OAuth2

            **Security:**
            - State parameter is automatically managed by Spring Security for CSRF protection
            - PKCE (Proof Key for Code Exchange) is used when supported by the provider
            """,
        parameters = {
            @Parameter(
                name = "provider",
                description = "OAuth2 provider identifier (google or github)",
                required = true,
                in = ParameterIn.PATH,
                examples = {
                    @ExampleObject(
                        name = "Google",
                        value = "google"
                    ),
                    @ExampleObject(
                        name = "GitHub",
                        value = "github"
                    )
                }
            )
        }
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "302",
                description = "Redirect to OAuth2 provider's authorization page",
                content = @Content(
                    mediaType = MediaType.TEXT_HTML_VALUE,
                    schema = @Schema(implementation = String.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid provider or configuration error",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject("""
                        {
                          "error": "invalid_request",
                          "error_description": "Invalid OAuth2 provider"
                        }
                        """)
                )
            )
        }
    )
    @GetMapping(OAUTH2_AUTHORIZATION_PROVIDER_PATH)
    public void initiateOAuth2Login(@PathVariable final String provider) {
        // This method is never invoked - Spring Security intercepts the request
        // It exists only for OpenAPI documentation purposes
        throw new UnsupportedOperationException(SPRING_SECURITY_HANDLED_MESSAGE);
    }

    /**
     * OAuth2 callback endpoint that receives the authorization code from the provider.
     * <p>
     * This endpoint is called by the OAuth2 provider after successful user authorization.
     * Spring Security automatically handles the callback, exchanges the authorization code
     * for access tokens, retrieves user information, and processes authentication.
     *
     * @param provider The OAuth2 provider (google or github)
     */
    @Operation(
        summary = "OAuth2 callback endpoint",
        description = """
            Receives the OAuth2 authorization callback from the provider.

            **This endpoint is automatically called by the OAuth2 provider - users should not call it directly.**

            **Callback Flow:**
            1. Provider redirects to this endpoint with authorization code and state
            2. Spring Security validates the state parameter
            3. Authorization code is exchanged for access token
            4. User information is retrieved from provider's user info endpoint
            5. User is created or updated in the database
            6. JWT access and refresh tokens are generated
            7. Tokens are set as HTTP-only, secure cookies
            8. User is redirected to the frontend application

            **On Success:**
            - Redirects to: `/api/v1/oauth2/redirect` with authentication cookies set
            - Cookies include: access_token, refresh_token (HTTP-only, Secure, SameSite=Lax)

            **On Failure:**
            - Redirects to: `/api/v1/auth/login?error={error_message}`
            - Error parameter contains a simple error message describing the failure
            - Frontend can display the error message to the user

            **Security Measures:**
            - State validation prevents CSRF attacks
            - Tokens are stored in HTTP-only cookies to prevent XSS
            - Email verification is checked from provider data
            - User accounts are automatically created/linked based on email
            """,
        parameters = {
            @Parameter(
                name = "provider",
                description = "OAuth2 provider identifier (must match the initiating provider)",
                required = true,
                in = ParameterIn.PATH,
                examples = {
                    @ExampleObject(
                        name = "Google",
                        value = "google"
                    ),
                    @ExampleObject(
                        name = "GitHub",
                        value = "github"
                    )
                }
            ),
            @Parameter(
                name = "code",
                description = "Authorization code provided by the OAuth2 provider",
                required = true,
                in = ParameterIn.QUERY
            ),
            @Parameter(
                name = "state",
                description = "State parameter for CSRF protection (automatically validated by Spring Security)",
                required = true,
                in = ParameterIn.QUERY
            )
        }
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "302",
                description = "Successful authentication - redirects to frontend with cookies set",
                content = @Content(
                    mediaType = MediaType.TEXT_HTML_VALUE,
                    schema = @Schema(implementation = String.class)
                ),
                headers = {
                    @Header(
                        name = "Set-Cookie",
                        description = "HTTP-only cookies containing JWT access and refresh tokens",
                        schema = @Schema(type = "string")
                    ),
                    @Header(
                        name = "Location",
                        description = "Redirect URL to frontend application (/api/oauth2/redirect)",
                        schema = @Schema(type = "string")
                    )
                }
            ),
            @ApiResponse(
                responseCode = "302",
                description = "Authentication failed - redirects to error page",
                content = @Content(
                    mediaType = MediaType.TEXT_HTML_VALUE
                ),
                headers = {
                    @Header(
                        name = "Location",
                        description = "Redirect URL to login page with error details (/api/v1/auth/login?error=...)",
                        schema = @Schema(type = "string")
                    )
                }
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid callback parameters (missing code or state)",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject("""
                        {
                          "error": "invalid_request",
                          "error_description": "Missing required parameter: code"
                        }
                        """)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid or expired authorization code",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject("""
                        {
                          "error": "invalid_grant",
                          "error_description": "Authorization code is invalid or expired"
                        }
                        """)
                )
            )
        }
    )
    @GetMapping(OAUTH2_CALLBACK_PATH)
    public void handleOAuth2Callback(
        @PathVariable final String provider,
        @Parameter(hidden = true) final String code,
        @Parameter(hidden = true) final String state) {
        // This method is never invoked - Spring Security intercepts the request
        // It exists only for OpenAPI documentation purposes
        throw new UnsupportedOperationException(SPRING_SECURITY_HANDLED_MESSAGE);
    }
}
