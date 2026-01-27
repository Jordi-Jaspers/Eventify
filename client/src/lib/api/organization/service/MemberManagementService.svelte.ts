import type {
	OrganizationMembershipResponse,
	OrganizationalRole,
	UserResponse
} from '$lib/api/models';
import {
	searchUsersToAdd,
	addMember,
	updateMemberRole,
	removeMember,
	transferOwnership
} from '../OrganizationMembershipController';
import { assignOrganizationOwner } from '$lib/api/admin/AdminController';
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';

/**
 * Service for managing organization member operations.
 * Encapsulates search, add, update, remove, and transfer logic.
 */
export function createMemberManagementService(orgId: number, onMutationSuccess: () => void) {
	// Add member state
	let searchQuery: string = $state('');
	let debouncedQuery: string = $state('');
	let isSearching: boolean = $state(false);
	let searchResults: UserResponse[] = $state([]);
	let showSearchDropdown: boolean = $state(false);
	let debounceTimer: ReturnType<typeof setTimeout> | null = null;
	let selectedUser: UserResponse | null = $state(null);
	let selectedRole: OrganizationalRole = $state('MEMBER');
	let isAdding: boolean = $state(false);

	// Remove member state
	let memberToRemove: OrganizationMembershipResponse | null = $state(null);
	let isRemoving: boolean = $state(false);

	// Transfer state
	let transferTarget: OrganizationMembershipResponse | null = $state(null);
	let transferConfirmation: string = $state('');
	let isTransferring: boolean = $state(false);

	// Search functions
	async function performSearch(query: string): Promise<void> {
		isSearching = true;
		showSearchDropdown = true;
		try {
			searchResults = await searchUsersToAdd(orgId, query);
		} catch (err: unknown) {
			console.error('Search error:', err);
			searchResults = [];
		} finally {
			isSearching = false;
		}
	}

	function setSearchQuery(query: string): void {
		searchQuery = query;
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
			debounceTimer = setTimeout(() => {
				debouncedQuery = query;
				performSearch(query);
			}, 300);
		} else {
			showSearchDropdown = true;
			debouncedQuery = '';
			searchResults = [];
		}
	}

	function selectUser(user: UserResponse): void {
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

	// Mutation handlers
	async function handleAdd(): Promise<void> {
		if (!selectedUser?.email || !selectedRole) {
			toast.error('Please select a user and role');
			return;
		}
		isAdding = true;
		try {
			if (selectedRole === 'OWNER') {
				await assignOrganizationOwner(orgId, { email: selectedUser.email });
			} else {
				await addMember(orgId, { email: selectedUser.email, role: selectedRole });
			}
			toast.success(`${selectedUser.email} added successfully`);
			resetAddState();
			onMutationSuccess();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to add member');
			toast.error(message);
		} finally {
			isAdding = false;
		}
	}

	async function handleUpdateRole(
		member: OrganizationMembershipResponse,
		newRole: OrganizationalRole
	): Promise<void> {
		if (member.role === newRole || member.role === 'OWNER') return;
		if (!member.userId) return;
		try {
			await updateMemberRole(orgId, member.userId, { role: newRole });
			toast.success(`Role updated to ${newRole}`);
			onMutationSuccess();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to update role');
			toast.error(message);
		}
	}

	async function handleRemove(): Promise<void> {
		if (!memberToRemove || !memberToRemove.userId) return;
		isRemoving = true;
		try {
			await removeMember(orgId, memberToRemove.userId);
			toast.success(`${memberToRemove.userEmail ?? 'Member'} removed successfully`);
			memberToRemove = null;
			onMutationSuccess();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to remove member');
			toast.error(message);
		} finally {
			isRemoving = false;
		}
	}

	async function handleTransfer(currentOwner: OrganizationMembershipResponse): Promise<void> {
		if (!transferTarget || transferConfirmation !== 'Transfer Ownership') {
			toast.error('Please type "Transfer Ownership" to confirm');
			return;
		}
		if (!currentOwner.userId || !transferTarget.userId) {
			toast.error('Could not find current owner');
			return;
		}
		isTransferring = true;
		try {
			await transferOwnership(orgId, {
				currentOwnerUserId: currentOwner.userId,
				newOwnerUserId: transferTarget.userId
			});
			toast.success(`Ownership transferred to ${transferTarget.userEmail ?? 'new owner'}`);
			transferTarget = null;
			transferConfirmation = '';
			onMutationSuccess();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to transfer ownership');
			toast.error(message);
		} finally {
			isTransferring = false;
		}
	}

	// Sheet state setters
	function setMemberToRemove(member: OrganizationMembershipResponse | null): void {
		memberToRemove = member;
	}

	function setTransferTarget(member: OrganizationMembershipResponse | null): void {
		transferTarget = member;
	}

	function setTransferConfirmation(value: string): void {
		transferConfirmation = value;
	}

	function setSelectedRole(role: OrganizationalRole): void {
		selectedRole = role;
	}

	function setShowSearchDropdown(show: boolean): void {
		showSearchDropdown = show;
	}

	return {
		// State getters
		get searchQuery(): string {
			return searchQuery;
		},
		get debouncedQuery(): string {
			return debouncedQuery;
		},
		get isSearching(): boolean {
			return isSearching;
		},
		get searchResults(): UserResponse[] {
			return searchResults;
		},
		get showSearchDropdown(): boolean {
			return showSearchDropdown;
		},
		get selectedUser(): UserResponse | null {
			return selectedUser;
		},
		get selectedRole(): OrganizationalRole {
			return selectedRole;
		},
		get isAdding(): boolean {
			return isAdding;
		},
		get memberToRemove(): OrganizationMembershipResponse | null {
			return memberToRemove;
		},
		get isRemoving(): boolean {
			return isRemoving;
		},
		get transferTarget(): OrganizationMembershipResponse | null {
			return transferTarget;
		},
		get transferConfirmation(): string {
			return transferConfirmation;
		},
		get isTransferring(): boolean {
			return isTransferring;
		},

		// Actions
		setSearchQuery,
		selectUser,
		clearUserSelection,
		resetAddState,
		handleAdd,
		handleUpdateRole,
		handleRemove,
		handleTransfer,
		setMemberToRemove,
		setTransferTarget,
		setTransferConfirmation,
		setSelectedRole,
		setShowSearchDropdown
	};
}

export type MemberManagementService = ReturnType<typeof createMemberManagementService>;
