import {client} from "$lib/api/client.ts";
import type {
    OrganizationResponse,
    OrganizationStatus,
    PageResourceOrganizationResponse,
    SearchInput,
    SortablePageInput,
    SortDirection
} from "$lib/api/models.ts";

export interface SearchOrganizationsParams {
    page?: number;
    size?: number;
    search?: string;
    status?: OrganizationStatus;
    sortKey?: string;
    sortDirection?: SortDirection;
}

/**
 * Create a new organization (Admin only)
 */
export async function createOrganization(name: string, owner: string): Promise<OrganizationResponse> {
    const {data, error} = await client.POST('/v1/admin/organization', {
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
        searchInputs,
        sortOrder: params.sortKey && params.sortDirection ? [{
            name: params.sortKey,
            direction: params.sortDirection
        }] : undefined
    };

    const {data, error} = await client.POST('/v1/admin/organization/search', {
        body: requestBody
    });

    if (error) {
        throw error;
    }

    return data;
}
