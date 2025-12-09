import {client} from "$lib/api/client.ts";
import type {AuthenticationResponse} from "$lib/api/models.ts";

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

