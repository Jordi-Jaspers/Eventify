import {client} from "$lib/api/client.ts";
import type {OrganizationResponse} from "$lib/api/models.ts";

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
