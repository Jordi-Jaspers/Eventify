import { client } from '$lib/api/client.ts';
import type { SessionResponse } from '$lib/api/models.ts';

/**
 * List all active sessions for the current user
 */
export async function listSessions(): Promise<SessionResponse[]> {
    const { data, error } = await client.GET('/v1/user/sessions');

    if (error) {
        throw error;
    }

    return data;
}

/**
 * Revoke a specific session by ID
 */
export async function revokeSession(sessionId: number): Promise<void> {
    const { error } = await client.DELETE('/v1/user/sessions/{id}', {
        params: { path: { id: sessionId } }
    });

    if (error) {
        throw error;
    }
}

/**
 * Revoke all sessions except the current one
 */
export async function revokeAllOtherSessions(): Promise<void> {
    const { error } = await client.DELETE('/v1/user/sessions');

    if (error) {
        throw error;
    }
}
