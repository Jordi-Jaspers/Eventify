import { lockUser, unlockUser, updateUserRole, forcePasswordReset } from '../AdminUserController';
import type { UserDetailsResponse } from '$lib/api/models';
import { toast } from 'svelte-sonner';
import { handleError } from '$lib/utils/error-handler';

/**
 * Admin User Service
 * 
 * Manages user administration operations (lock/unlock, role changes, password resets)
 */
export class AdminUserService {
	// Loading states
	updatingRole = $state<boolean>(false);
	lockingUser = $state<boolean>(false);
	forcingPasswordReset = $state<boolean>(false);

	/**
	 * Update a user's role (USER or ADMIN)
	 */
	async updateRole(userId: number | undefined, newRole: 'USER' | 'ADMIN'): Promise<UserDetailsResponse | null> {
		if (!userId || this.updatingRole) return null;

		this.updatingRole = true;
		try {
			const updated: UserDetailsResponse = await updateUserRole(userId, newRole);
			toast.success(`Role updated to ${newRole}`);
			return updated;
		} catch (error: unknown) {
			const { message }: { message: string } = handleError(error, 'Failed to update role');
			toast.error(message);
			return null;
		} finally {
			this.updatingRole = false;
		}
	}

	/**
	 * Lock or unlock a user account
	 */
	async toggleLock(userId: number | undefined, isLocked: boolean): Promise<UserDetailsResponse | null> {
		if (!userId || this.lockingUser) return null;

		this.lockingUser = true;
		try {
			const updated: UserDetailsResponse = isLocked 
				? await unlockUser(userId) 
				: await lockUser(userId);
			toast.success(isLocked ? 'User unlocked' : 'User locked');
			return updated;
		} catch (error: unknown) {
			const { message }: { message: string } = handleError(
				error, 
				isLocked ? 'Failed to unlock user' : 'Failed to lock user'
			);
			toast.error(message);
			return null;
		} finally {
			this.lockingUser = false;
		}
	}

	/**
	 * Lock a user account (convenience method)
	 */
	async lock(userId: number | undefined): Promise<UserDetailsResponse | null> {
		return this.toggleLock(userId, false);
	}

	/**
	 * Unlock a user account (convenience method)
	 */
	async unlock(userId: number | undefined): Promise<UserDetailsResponse | null> {
		return this.toggleLock(userId, true);
	}

	/**
	 * Force a password reset for a user due to security concerns.
	 * Immediately invalidates the current password and sends a reset email.
	 */
	async forcePasswordReset(userId: number | undefined, email: string | undefined): Promise<void> {
		if (!userId || !email || this.forcingPasswordReset) return;

		this.forcingPasswordReset = true;
		try {
			await forcePasswordReset(userId);
			toast.success(`Password invalidated. Reset email sent to ${email}`);
		} catch (error: unknown) {
			const { message }: { message: string } = handleError(error, 'Failed to force password reset');
			toast.error(message);
		} finally {
			this.forcingPasswordReset = false;
		}
	}
}

/**
 * Create an AdminUserService instance
 */
export function createAdminUserService(): AdminUserService {
	return new AdminUserService();
}
