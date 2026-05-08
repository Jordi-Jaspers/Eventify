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

export interface UpdatePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

/**
 * Update password while authenticated (requires current password)
 */
export async function updatePassword(request: UpdatePasswordRequest): Promise<void> {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const { error } = await (client as any).POST('/v1/password/update-password', {
        body: request
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
