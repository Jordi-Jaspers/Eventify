import {client} from "$lib/api/client.ts";
import type {
    AuthenticationResponse,
    LoginRequest,
    OAuthProvider,
    RegisterRequest,
    RegisterResponse
} from "$lib/api/models.ts";
import {buildOAuth2Url} from "$lib/api/authentication/oauth2-url.ts";

/**
 * Initiates OAuth2 login flow by redirecting to the backend OAuth endpoint.
 * The backend will then redirect to the provider's authorization page.
 *
 * @param provider - OAuth provider (google or github)
 */
export function initiateOAuth(provider: OAuthProvider): void {
    globalThis.location.href = buildOAuth2Url(provider);
}

/**
 * Login with email and password
 */
export async function login(request: LoginRequest): Promise<AuthenticationResponse> {
    const {data, error} = await client.POST('/v1/auth/login', {
        body: request
    });

    if (error) {
        throw error;
    }
    return data;
}

/**
 * Register a new user
 */
export async function register(request: RegisterRequest): Promise<RegisterResponse> {
    const {data, error} = await client.POST('/v1/auth/register', {
        body: request
    });

    if (error) {
        throw error;
    }
    return data;
}

/**
 * Verify email with token
 */
export async function verifyEmail(token: string): Promise<AuthenticationResponse> {
    const {data, error} = await client.POST('/v1/auth/verify', {
        params: {
            query: {
                token
            }
        }
    });

    if (error) {
        throw error;
    }
    return data;
}

/**
 * Resend verification email
 */
export async function resendVerification(email: string): Promise<void> {
    const {error} = await client.POST('/v1/auth/verify/resend', {
        params: {
            query: {
                email
            }
        }
    });

    if (error) {
        throw error;
    }
}

/**
 * Logout the current user
 */
export async function logout(): Promise<void> {
    const {error} = await client.GET('/v1/auth/logout');
    if (error) {
        throw error;
    }
}
