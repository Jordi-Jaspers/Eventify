import { toast } from 'svelte-sonner';
import {
	getOrganizationMembers,
	addMember,
	updateMemberRole,
	removeMember,
	transferOwnership,
	searchUsersToAdd
} from './OrganizationMembershipController';
import { assignOrganizationOwner } from '$lib/api/admin/AdminController';
import { handleError } from '$lib/utils/error-handler';
import type {
	OrganizationMembershipResponse,
	OrganizationalRole,
	UserSearchResult
} from '$lib/api/models';

/**
 * Service for managing organization membership operations.
 * Encapsulates business logic, state management, and error handling.
 */
export function createMembershipService(orgId: number) {
	// Core state
	let members: OrganizationMembershipResponse[] = $state([]);
	let loading: boolean = $state(true);
	let error: string | null = $state(null);

	// Search state
	let searchQuery: string = $state('');
	let debouncedQuery: string = $state('');
	let isSearching: boolean = $state(false);
	let searchResults: UserSearchResult[] = $state([]);
	let showSearchDropdown: boolean = $state(false);
	let debounceTimer: ReturnType<typeof setTimeout> | null = null;

	// Selected user for adding
	let selectedUser: UserSearchResult | null = $state(null);
	let selectedRole: OrganizationalRole = $state('MEMBER');

	// Operation states
	let isAdding: boolean = $state(false);
	let isRemoving: boolean = $state(false);
	let isTransferring: boolean = $state(false);

	// Member targets for sheets
	let memberToRemove: OrganizationMembershipResponse | null = $state(null);
	let transferTarget: OrganizationMembershipResponse | null = $state(null);
	let transferConfirmation: string = $state('');

	async function performSearch(query: string): Promise<void> {
		isSearching = true;
		showSearchDropdown = true;

		try {
			const results: UserSearchResult[] = await searchUsersToAdd(orgId, query);
			const memberEmails: Set<string> = new Set(
				members.map((m: OrganizationMembershipResponse) => m.userEmail.toLowerCase())
			);
			searchResults = results.filter((user: UserSearchResult) => {
				const email: string | undefined = user.email;
				return email !== undefined && !memberEmails.has(email.toLowerCase());
			});
		} catch (err: unknown) {
			console.error('Search error:', err);
			searchResults = [];
		} finally {
			isSearching = false;
		}
	}

	async function loadMembers(): Promise<void> {
		loading = true;
		error = null;

		try {
			members = await getOrganizationMembers(orgId);
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load members');
			error = message;
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	async function add(): Promise<boolean> {
		if (!selectedUser?.email || !selectedRole) {
			toast.error('Please select a user and role');
			return false;
		}

		isAdding = true;

		try {
			let newMember: OrganizationMembershipResponse;
			
			if (selectedRole === 'OWNER') {
				// Use admin endpoint for OWNER role
				newMember = await assignOrganizationOwner(orgId, {
					email: selectedUser.email
				});
			} else {
				// Use regular endpoint for MEMBER/ADMIN roles
				newMember = await addMember(orgId, {
					email: selectedUser.email,
					role: selectedRole
				});
			}
			
			members = [...members, newMember];
			toast.success(`${selectedUser.email} added successfully`);
			resetAddState();
			return true;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to add member');
			toast.error(message);
			return false;
		} finally {
			isAdding = false;
		}
	}

	async function updateRole(
		member: OrganizationMembershipResponse,
		newRole: OrganizationalRole
	): Promise<void> {
		if (member.role === newRole || member.role === 'OWNER') {
			return;
		}

		const oldRole: OrganizationalRole = member.role;
		const memberIndex: number = members.findIndex(
			(m: OrganizationMembershipResponse) => m.id === member.id
		);

		// Optimistic update
		if (memberIndex >= 0) {
			members[memberIndex].role = newRole;
			members = [...members];
		}

		try {
			await updateMemberRole(orgId, member.userId, { role: newRole });
			toast.success(`Role updated to ${newRole}`);
		} catch (err: unknown) {
			// Rollback on error
			if (memberIndex >= 0) {
				members[memberIndex].role = oldRole;
				members = [...members];
			}
			const { message }: { message: string } = handleError(err, 'Failed to update role');
			toast.error(message);
		}
	}

	async function remove(): Promise<boolean> {
		if (!memberToRemove) return false;

		isRemoving = true;

		try {
			await removeMember(orgId, memberToRemove.userId);
			members = members.filter(
				(m: OrganizationMembershipResponse) => m.id !== memberToRemove?.id
			);
			toast.success(`${memberToRemove.userEmail} removed successfully`);
			memberToRemove = null;
			return true;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to remove member');
			toast.error(message);
			return false;
		} finally {
			isRemoving = false;
		}
	}

	async function transfer(): Promise<boolean> {
		if (!transferTarget || transferConfirmation !== 'Transfer Ownership') {
			toast.error('Please type "Transfer Ownership" to confirm');
			return false;
		}

		// Find the current owner from members list
		const currentOwner: OrganizationMembershipResponse | undefined = members.find(
			(m: OrganizationMembershipResponse) => m.role === 'OWNER'
		);
		if (!currentOwner) {
			toast.error('Could not find current owner');
			return false;
		}

		isTransferring = true;

		try {
			await transferOwnership(orgId, {
				currentOwnerUserId: currentOwner.userId,
				newOwnerUserId: transferTarget.userId
			});
			await loadMembers();
			toast.success(`Ownership transferred to ${transferTarget.userEmail}`);
			resetTransferState();
			return true;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to transfer ownership');
			toast.error(message);
			return false;
		} finally {
			isTransferring = false;
		}
	}

	function selectUser(user: UserSearchResult): void {
		selectedUser = user;
		searchQuery = '';
		debouncedQuery = '';
		searchResults = [];
		showSearchDropdown = false;
	}

	function clearUserSelection(): void {
		selectedUser = null;
		searchQuery = '';
		debouncedQuery = '';
		searchResults = [];
		showSearchDropdown = false;
	}

	function setSearchQuery(query: string): void {
		searchQuery = query;
		
		// Clear existing timer
		if (debounceTimer) {
			clearTimeout(debounceTimer);
			debounceTimer = null;
		}

		if (query.length === 0) {
			debouncedQuery = '';
			searchResults = [];
			showSearchDropdown = false;
		} else if (query.length >= 3) {
			showSearchDropdown = true;
			// Debounce the search
			debounceTimer = setTimeout(() => {
				debouncedQuery = query;
				performSearch(query);
			}, 300);
		} else {
			// 1-2 characters: show dropdown with message but don't search
			showSearchDropdown = true;
			debouncedQuery = '';
			searchResults = [];
		}
	}

	function showDropdown(): void {
		if (searchQuery.length > 0) {
			showSearchDropdown = true;
		}
	}

	function resetAddState(): void {
		selectedUser = null;
		selectedRole = 'MEMBER';
		searchQuery = '';
		debouncedQuery = '';
		searchResults = [];
		showSearchDropdown = false;
		if (debounceTimer) {
			clearTimeout(debounceTimer);
			debounceTimer = null;
		}
	}

	function setMemberToRemove(member: OrganizationMembershipResponse | null): void {
		memberToRemove = member;
	}

	function setTransferTarget(member: OrganizationMembershipResponse | null): void {
		transferTarget = member;
		transferConfirmation = '';
	}

	function setTransferConfirmation(value: string): void {
		transferConfirmation = value;
	}

	function resetTransferState(): void {
		transferTarget = null;
		transferConfirmation = '';
	}

	return {
		// Getters (reactive)
		get members() { return members; },
		get loading() { return loading; },
		get error() { return error; },
		get searchQuery() { return searchQuery; },
		get debouncedQueryLength() { return debouncedQuery.length; },
		get isSearching() { return isSearching; },
		get searchResults() { return searchResults; },
		get showSearchDropdown() { return showSearchDropdown; },
		get selectedUser() { return selectedUser; },
		get selectedRole() { return selectedRole; },
		get isAdding() { return isAdding; },
		get isRemoving() { return isRemoving; },
		get isTransferring() { return isTransferring; },
		get memberToRemove() { return memberToRemove; },
		get transferTarget() { return transferTarget; },
		get transferConfirmation() { return transferConfirmation; },

		// Setters
		set selectedRole(role: OrganizationalRole) { selectedRole = role; },

		// Actions
		loadMembers,
		add,
		updateRole,
		remove,
		transfer,
		selectUser,
		clearUserSelection,
		setSearchQuery,
		showDropdown,
		resetAddState,
		setMemberToRemove,
		setTransferTarget,
		setTransferConfirmation,
		resetTransferState
	};
}

export type MembershipService = ReturnType<typeof createMembershipService>;
