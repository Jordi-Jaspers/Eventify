import { client } from "$lib/api/client.ts";
import type { ForgotPasswordRequest } from "$lib/api/models.ts";

/**
 * Request a password reset email
 */
export async function requestPasswordReset(email: string): Promise<void> {
	const { error } = await client.POST('/v1/public/reset-password/request', {
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
 * Reset password with token
 */
export async function resetPassword(request: ForgotPasswordRequest): Promise<void> {
	const { error } = await client.POST('/v1/public/reset-password', {
		body: request
	});

	if (error) {
		throw error;
	}
}
