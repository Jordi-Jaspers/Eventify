import type {
	OrganizationMembershipResponse,
	AddMemberRequest,
	UpdateMemberRoleRequest,
	TransferOwnershipRequest,
	UserSearchResult,
	SortablePageInput
} from '$lib/api/models';
import {client} from "$lib/api/client.ts";

/**
 * Get all members of an organization
 */
export async function getOrganizationMembers(orgId: number): Promise<OrganizationMembershipResponse[]> {
	const {data, error} = await client.GET('/v1/organization/{orgId}/members', {
		params: { path: { orgId } }
	});

	if (error) {
		throw error;
	}

	// Cast from OpenAPI optional types to our required types
	// Backend always returns complete objects
	return (data ?? []) as OrganizationMembershipResponse[];
}

/**
 * Add a member to an organization
 */
export async function addMember(orgId: number, request: AddMemberRequest): Promise<OrganizationMembershipResponse> {
	const {data, error} = await client.POST('/v1/organization/{orgId}/members', {
		params: { path: { orgId } },
		body: request
	});

	if (error) {
		throw error;
	}

	if (!data) {
		throw new Error('No data returned from add member');
	}

	return data as OrganizationMembershipResponse;
}

/**
 * Update a member's role
 */
export async function updateMemberRole(orgId: number, userId: number, request: UpdateMemberRoleRequest): Promise<OrganizationMembershipResponse> {
	const {data, error} = await client.PATCH('/v1/organization/{orgId}/members/{userId}', {
		params: { path: { orgId, userId } },
		body: request
	});

	if (error) {
		throw error;
	}

	if (!data) {
		throw new Error('No data returned from update member role');
	}

	return data as OrganizationMembershipResponse;
}

/**
 * Remove a member from an organization
 */
export async function removeMember(orgId: number, userId: number): Promise<void> {
	const {error} = await client.DELETE('/v1/organization/{orgId}/members/{userId}', {
		params: { path: { orgId, userId } }
	});

	if (error) {
		throw error;
	}
}

/**
 * Transfer ownership of an organization
 */
export async function transferOwnership(orgId: number, request: TransferOwnershipRequest): Promise<void> {
	const {error} = await client.POST('/v1/organization/{orgId}/transfer-ownership', {
		params: { path: { orgId } },
		body: request
	});

	if (error) {
		throw error;
	}
}

/**
 * Search users to add to organization (min 3 characters)
 */
export async function searchUsersToAdd(orgId: number, query: string): Promise<UserSearchResult[]> {
	const requestBody: SortablePageInput = {
		pageNumber: 0,
		pageSize: 10,
		searchInputs: query
			? [
					{
						fieldName: 'search',
						textValue: query
					}
				]
			: []
	};

	const {data, error} = await client.POST('/v1/organization/{orgId}/members/search', {
		params: { path: { orgId } },
		body: requestBody
	});

	if (error) {
		throw error;
	}

	return (data?.content ?? []) as UserSearchResult[];
}
