import type { OrganizationalRole, UserRole } from '$lib/api/models';

// ================ Organizational Roles (OWNER, ADMIN, MEMBER) ==================

export function getOrganizationalRoleLabel(role: OrganizationalRole | undefined): string {
	switch (role) {
		case 'OWNER':
			return 'Owner';
		case 'ADMIN':
			return 'Admin';
		case 'MEMBER':
			return 'Member';
		default:
			return role ?? '';
	}
}

/**
 * Badge classes for organizational roles - consistent styling across the app
 * OWNER: Purple gradient with white text
 * ADMIN: Blue tinted background with blue text
 * MEMBER: Muted/neutral styling
 */
export function getOrganizationalRoleBadgeClass(role: OrganizationalRole | undefined): string {
	switch (role) {
		case 'OWNER':
			return 'bg-gradient-to-r from-purple-500 to-purple-600 border-0 text-white';
		case 'ADMIN':
			return 'bg-blue-500/10 border-blue-500/50 text-blue-500';
		case 'MEMBER':
		default:
			return 'border-border/50 bg-background/50 text-muted-foreground';
	}
}

// ================ User Roles (USER, ADMIN) ==================

export function getUserRoleLabel(role: UserRole | undefined): string {
	switch (role) {
		case 'ADMIN':
			return 'Admin';
		case 'USER':
			return 'User';
		default:
			return role ?? '';
	}
}

/**
 * Badge classes for user roles - consistent styling across the app
 * ADMIN: Blue tinted background with blue text
 * USER: Muted/neutral styling
 */
export function getUserRoleBadgeClass(role: UserRole | undefined): string {
	switch (role) {
		case 'ADMIN':
			return 'bg-blue-500/10 border-blue-500/50 text-blue-500';
		case 'USER':
		default:
			return 'border-border/50 bg-background/50 text-muted-foreground';
	}
}
