import { SERVER_BASE_URL } from '$lib/config/constants';
import type {
	OrganizationMembershipResponse,
	AddMemberRequest,
	UpdateMemberRoleRequest,
	TransferOwnershipRequest,
	UserSearchResult,
	SortablePageInput
} from '$lib/api/models';

/**
 * Get all members of an organization
 */
export async function getOrganizationMembers(
	orgId: number
): Promise<OrganizationMembershipResponse[]> {
	const response: Response = await fetch(`${SERVER_BASE_URL}/v1/organizations/${orgId}/members`, {
		method: 'GET',
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json'
		}
	});

	if (!response.ok) {
		throw new Error(`Failed to fetch organization members: ${response.statusText}`);
	}

	return await response.json();
}

/**
 * Add a member to an organization
 */
export async function addMember(
	orgId: number,
	request: AddMemberRequest
): Promise<OrganizationMembershipResponse> {
	const response: Response = await fetch(`${SERVER_BASE_URL}/v1/organizations/${orgId}/members`, {
		method: 'POST',
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(request)
	});

	if (!response.ok) {
		throw new Error(`Failed to add member: ${response.statusText}`);
	}

	return await response.json();
}

/**
 * Update a member's role
 */
export async function updateMemberRole(
	orgId: number,
	userId: number,
	request: UpdateMemberRoleRequest
): Promise<OrganizationMembershipResponse> {
	const response: Response = await fetch(
		`${SERVER_BASE_URL}/v1/organizations/${orgId}/members/${userId}`,
		{
			method: 'PATCH',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(request)
		}
	);

	if (!response.ok) {
		throw new Error(`Failed to update member role: ${response.statusText}`);
	}

	return await response.json();
}

/**
 * Remove a member from an organization
 */
export async function removeMember(orgId: number, userId: number): Promise<void> {
	const response: Response = await fetch(
		`${SERVER_BASE_URL}/v1/organizations/${orgId}/members/${userId}`,
		{
			method: 'DELETE',
			credentials: 'include'
		}
	);

	if (!response.ok) {
		throw new Error(`Failed to remove member: ${response.statusText}`);
	}
}

/**
 * Transfer ownership of an organization
 */
export async function transferOwnership(
	orgId: number,
	request: TransferOwnershipRequest
): Promise<void> {
	const response: Response = await fetch(
		`${SERVER_BASE_URL}/v1/organizations/${orgId}/transfer-ownership`,
		{
			method: 'POST',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(request)
		}
	);

	if (!response.ok) {
		throw new Error(`Failed to transfer ownership: ${response.statusText}`);
	}
}

/**
 * Search users to add to organization (min 3 characters)
 * Uses admin user search endpoint since org-specific search doesn't return user IDs
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

	const response: Response = await fetch(`${SERVER_BASE_URL}/admin/users/search`, {
		method: 'POST',
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(requestBody)
	});

	if (!response.ok) {
		throw new Error(`Failed to search users: ${response.statusText}`);
	}

	const data = await response.json();
	return data.content ?? [];
}
