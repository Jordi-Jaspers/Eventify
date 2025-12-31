import {client} from "$lib/api/client.ts";
import {SERVER_BASE_URL} from "$lib/config/constants.ts";
import type {
    OrganizationResponse,
    OrganizationStatus,
    PageResourceOrganizationResponse,
    SearchInput,
    SortablePageInput
} from "$lib/api/models.ts";

export interface SearchOrganizationsParams {
    page?: number;
    size?: number;
    search?: string;
    status?: OrganizationStatus;
}

/**
 * Create a new organization (Admin only)
 */
export async function createOrganization(name: string, owner: string): Promise<OrganizationResponse> {
    const {data, error} = await client.POST('/admin/organizations', {
        body: {
            name,
            owner
        }
    });

    if (error) {
        throw error;
    }

    return data;
}

/**
 * Search organizations with pagination and filtering (Admin only)
 * Uses Jframe SortablePageInput pattern with POST request
 */
export async function searchOrganizations(params: SearchOrganizationsParams = {}): Promise<PageResourceOrganizationResponse> {
    const searchInputs: SearchInput[] = [];

    if (params.search) {
        searchInputs.push({
            fieldName: 'name',
            textValue: params.search
        });
    }

    if (params.status) {
        searchInputs.push({
            fieldName: 'status',
            textValueList: [params.status]
        });
    }

    const requestBody: SortablePageInput = {
        pageNumber: params.page ?? 0,
        pageSize: params.size ?? 10,
        searchInputs
    };

    const response: Response = await fetch(`${SERVER_BASE_URL}/admin/organizations/search`, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody)
    });

    if (!response.ok) {
        const errorData: unknown = await response.json().catch(() => ({}));
        throw errorData;
    }

    return await response.json();
}
