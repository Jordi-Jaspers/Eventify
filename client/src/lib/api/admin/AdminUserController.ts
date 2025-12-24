import { client } from '$lib/api/client.ts';
import type {UserSearchResult} from "$lib/api/models.ts";

/**
 * Search for users by email, first name, or last name
 * @param query Search query (min 3 characters)
 * @returns List of matching users (max 10)
 */
export async function searchUsers(query: string): Promise<UserSearchResult[]> {
	const { data, error } = await client.GET('/admin/users/search', {
		params: { query: { query } }
	});

	if (error) {
		throw error;
	}

	return data ?? [];
}
