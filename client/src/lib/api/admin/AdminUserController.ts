import {client} from '$lib/api/client.ts';
import type {PageResource, SearchInput, SortablePageInput, UserDetailsResponse} from '$lib/api/models.ts';

/**
 * Search paginated for users by email, first name, or last name
 * @param input SortablePageInput with pageNumber, pageSize, and optional search query
 * @returns Paginated list of matching users
 */
export async function searchUsers(input: SortablePageInput): Promise<PageResource<UserDetailsResponse>> {
    const {data, error} = await client.POST('/v1/admin/user/search', {
        body: input
    });

    if (error) {
        throw error;
    }

    // Backend returns UserDetailsResponse but OpenAPI spec says UserResponse - cast it
    return data as unknown as PageResource<UserDetailsResponse>;
}

/**
 * Search for users by email, first name, or last name (without pagination)
 * @param query Search query (min 3 characters)
 * @returns List of matching users (max 50)
 */
export async function searchUsersByEmailAndName(query: string): Promise<UserDetailsResponse[]> {
    const searchInputs: SearchInput[] = [];
    if (query) {
        searchInputs.push({
            fieldName: 'search',
            textValue: query
        });
    }

    const requestBody: SortablePageInput = {
        pageNumber: 0,
        pageSize: 10,
        searchInputs
    };

    const data: PageResource<UserDetailsResponse> = await searchUsers(requestBody);
    return data.content ?? [];
}

/**
 * Lock a user account
 * @param userId The user ID to lock
 * @returns Updated user details
 */
export async function lockUser(userId: number): Promise<UserDetailsResponse> {
    const { data, error } = await client.POST('/v1/user/{id}/lock', {
        params: { path: { id: userId } }
    });

    if (error) {
        throw error;
    }

    return data;
}

/**
 * Unlock a user account
 * @param userId The user ID to unlock
 * @returns Updated user details
 */
export async function unlockUser(userId: number): Promise<UserDetailsResponse> {
    const { data, error } = await client.POST('/v1/user/{id}/unlock', {
        params: { path: { id: userId } }
    });

    if (error) {
        throw error;
    }

    return data;
}

/**
 * Update a user's role
 * @param userId The user ID
 * @param role The new role (USER or ADMIN)
 * @returns Updated user details
 */
export async function updateUserRole(userId: number, role: 'USER' | 'ADMIN'): Promise<UserDetailsResponse> {
    const { data, error } = await client.POST('/v1/user/{id}', {
        params: { path: { id: userId } },
        body: { role }
    });

    if (error) {
        throw error;
    }

    return data;
}
