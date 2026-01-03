import {client} from "$lib/api/client.ts";
import type {
    OrganizationResponse,
    PageResourceOrganizationResponse,
    SortablePageInput
} from "$lib/api/models.ts";

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
 * Search organizations with SortablePageInput (Admin only)
 * Used by DataTable component for server-side pagination, sorting, and filtering.
 */
export async function searchOrganizations(input: SortablePageInput): Promise<PageResourceOrganizationResponse> {
    const { data, error } = await client.POST('/v1/admin/organization/search', {
        body: input
    });

    if (error) {
        throw error;
    }

    return data;
}
