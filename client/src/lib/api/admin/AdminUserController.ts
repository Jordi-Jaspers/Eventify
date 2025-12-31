import {client} from '$lib/api/client.ts';
import type {PageResourceUserResponse, SearchInput, SortablePageInput, UserResponse} from '$lib/api/models.ts';

/**
 * Search paginated for users by email, first name, or last name
 * @param input SortablePageInput with pageNumber, pageSize, and optional search query
 * @returns Paginated list of matching users
 */
export async function searchUsers(input: SortablePageInput): Promise<PageResourceUserResponse> {
    const {data, error} = await client.POST('/admin/users/search', {
        body: input
    });

    if (error) {
        throw error;
    }

    return data;
}

/**
 * Search for users by email, first name, or last name (without pagination)
 * @param query Search query (min 3 characters)
 * @returns List of matching users (max 50)
 */
export async function searchUsersByEmailAndName(query: string): Promise<UserResponse[]> {
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

    const data: PageResourceUserResponse = await searchUsers(requestBody);
    return data.content ?? [];
}
