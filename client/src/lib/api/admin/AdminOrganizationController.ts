import { client } from '$lib/api/client';
import type {
    OrganizationResponse,
    OrganizationStatus,
    PageResourceOrganizationResponse,
    SortablePageInput
} from '$lib/api/models';

export async function createOrganization(name: string, owner: string): Promise<OrganizationResponse> {
    const { data, error } = await client.POST('/v1/admin/organization', {
        body: { name, owner }
    });
    if (error) throw error;
    return data;
}

export async function searchOrganizations(input: SortablePageInput): Promise<PageResourceOrganizationResponse> {
    const { data, error } = await client.POST('/v1/admin/organization/search', {
        body: input
    });
    if (error) throw error;
    return data;
}

export async function updateOrganizationStatus(orgId: number, status: OrganizationStatus): Promise<OrganizationResponse> {
    const { data, error } = await client.PATCH('/v1/admin/organization/{orgId}/status', {
        params: { path: { orgId } },
        body: { status }
    });
    if (error) throw error;
    return data;
}

export async function getOrganizationById(orgId: number): Promise<OrganizationResponse | null> {
    const { data, error } = await client.POST('/v1/admin/organization/search', {
        body: {
            pageNumber: 0,
            pageSize: 1,
            searchInputs: [{ fieldName: 'id', textValueAsInteger: orgId }]
        }
    });
    if (error) throw error;
    const content: OrganizationResponse[] | undefined = data?.content;
    return content && content.length > 0 ? content[0] : null;
}
