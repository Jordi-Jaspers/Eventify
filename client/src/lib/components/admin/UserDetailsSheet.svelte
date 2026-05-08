<script lang="ts">
	import type { UserDetailsResponse } from '$lib/api/models';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import * as Sheet from '$lib/components/ui/sheet';
	import { UserCog, Lock, Unlock, Key, Building2 } from '@lucide/svelte';
	import { getInitials } from '$lib/utils/string';
	import { formatDate } from '$lib/utils/date';

	interface Props {
		open: boolean;
		user: UserDetailsResponse | null;
		updatingRole: boolean;
		lockingUser: boolean;
		forcingPasswordReset: boolean;
		onOpenChange: (open: boolean) => void;
		onRoleChange: (userId: number | undefined, role: 'USER' | 'ADMIN') => void;
		onLockToggle: (userId: number | undefined, lock: boolean) => void;
		onForcePasswordReset: (userId: number | undefined, email: string | undefined) => void;
	}

	const {
		open,
		user,
		updatingRole,
		lockingUser,
		forcingPasswordReset,
		onOpenChange,
		onRoleChange,
		onLockToggle,
		onForcePasswordReset
	}: Props = $props();

	// Helper functions
	function getStatusBadgeVariant(enabled: boolean | undefined, validated: boolean | undefined): 'success' | 'destructive' | 'default' {
		if (!enabled) return 'destructive';
		if (!validated) return 'default';
		return 'success';
	}

	function getStatusLabel(enabled: boolean | undefined, validated: boolean | undefined): string {
		if (!enabled) return 'Locked';
		if (!validated) return 'Pending Verification';
		return 'Active';
	}

	function formatLastLogin(lastLogin: string | null | undefined): string {
		if (!lastLogin) return 'Never';
		return formatDate(lastLogin);
	}

	function getUserInitials(u: UserDetailsResponse): string {
		const firstName: string = u.firstName ?? 'U';
		const lastName: string = u.lastName ?? 'U';
		return getInitials(firstName, lastName);
	}

	function getFullName(u: UserDetailsResponse): string {
		const firstName: string = u.firstName ?? '';
		const lastName: string = u.lastName ?? '';
		return `${firstName} ${lastName}`.trim() || 'Unknown User';
	}
</script>

<Sheet.Root {open} onOpenChange={onOpenChange}>
	<Sheet.Content class="w-full sm:max-w-md bg-background/95 backdrop-blur-xl border-border/50 overflow-y-auto p-0">
		{#if user}
			<!-- Hero Header with Gradient -->
			<div class="relative bg-gradient-to-br from-primary/20 via-accent/15 to-background/50 pt-8 pb-12 px-6 backdrop-blur-sm">
				<div class="absolute inset-0 bg-grid-white/5"></div>
				<div class="relative flex flex-col items-center text-center">
					<!-- Avatar -->
					<div class="h-20 w-20 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center shadow-lg shadow-primary/25 ring-4 ring-background">
						<span class="text-2xl font-bold text-white">
							{getUserInitials(user)}
						</span>
					</div>
					<!-- Name + Email -->
					<h2 class="mt-4 text-xl font-semibold">{getFullName(user)}</h2>
					<p class="text-sm text-muted-foreground mt-1">{user.email}</p>
					<!-- Status Badge -->
					<div class="mt-3">
						<Badge 
							variant={getStatusBadgeVariant(user.enabled, user.validated)}
							class="px-3 py-1"
						>
							{getStatusLabel(user.enabled, user.validated)}
						</Badge>
					</div>
				</div>
			</div>

			<!-- Content -->
			<div class="px-6 py-4 -mt-6 space-y-4">
				<!-- Role Card -->
				<div class="rounded-xl border border-border/50 bg-card/50 backdrop-blur-sm p-4 shadow-sm">
					<div class="flex items-center justify-between mb-3">
						<span class="text-sm font-medium text-muted-foreground">Role</span>
						<Badge variant="outline" class="font-mono text-xs">
							{user.role}
						</Badge>
					</div>
					<div class="flex gap-2">
						<Button
							variant={user.role === 'USER' ? 'default' : 'outline'}
							size="sm"
							class="flex-1"
							onclick={() => onRoleChange(user?.id, 'USER')}
							disabled={updatingRole || user.role === 'USER'}
						>
							<UserCog class="h-4 w-4 mr-1.5" />
							User
						</Button>
						<Button
							variant={user.role === 'ADMIN' ? 'default' : 'outline'}
							size="sm"
							class="flex-1"
							onclick={() => onRoleChange(user?.id, 'ADMIN')}
							disabled={updatingRole || user.role === 'ADMIN'}
						>
							<Key class="h-4 w-4 mr-1.5" />
							Admin
						</Button>
					</div>
				</div>

				<!-- Info Grid -->
				<div class="grid grid-cols-2 gap-3">
					<div class="rounded-lg border border-border/50 bg-card/30 backdrop-blur-sm p-3">
						<p class="text-xs text-muted-foreground uppercase tracking-wide">Created</p>
						<p class="text-sm font-medium mt-1">{formatDate(user.createdAt ?? '')}</p>
					</div>
					<div class="rounded-lg border border-border/50 bg-card/30 backdrop-blur-sm p-3">
						<p class="text-xs text-muted-foreground uppercase tracking-wide">Last Login</p>
						<p class="text-sm font-medium mt-1">{formatLastLogin(user.lastLogin)}</p>
					</div>
					<div class="rounded-lg border border-border/50 bg-card/30 backdrop-blur-sm p-3">
						<p class="text-xs text-muted-foreground uppercase tracking-wide">Email Verified</p>
						<p class="text-sm font-medium mt-1 flex items-center gap-1.5">
							{#if user.validated}
								<span class="h-2 w-2 rounded-full bg-green-500"></span>
								Verified
							{:else}
								<span class="h-2 w-2 rounded-full bg-yellow-500"></span>
								Pending
							{/if}
						</p>
					</div>
					<div class="rounded-lg border border-border/50 bg-card/30 backdrop-blur-sm p-3">
						<p class="text-xs text-muted-foreground uppercase tracking-wide">Account</p>
						<p class="text-sm font-medium mt-1 flex items-center gap-1.5">
							{#if user.enabled}
								<span class="h-2 w-2 rounded-full bg-green-500"></span>
								Active
							{:else}
								<span class="h-2 w-2 rounded-full bg-red-500"></span>
								Locked
							{/if}
						</p>
					</div>
				</div>

				<!-- Organizations -->
				{#if user?.organizations && user.organizations.length > 0}
					<div>
						<p class="text-xs text-muted-foreground uppercase tracking-wide mb-2">
							Organizations ({user.organizations.length})
						</p>
						<div class="space-y-2">
							{#each user.organizations as org}
								<div class="flex items-center gap-3 rounded-lg border border-border/50 bg-card/30 backdrop-blur-sm p-3">
									<div class="h-8 w-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
										<Building2 class="h-4 w-4 text-primary" />
									</div>
									<div class="flex-1 min-w-0">
										<p class="text-sm font-medium truncate">{org.organizationName}</p>
										<p class="text-xs text-muted-foreground">{org.role}</p>
									</div>
								</div>
							{/each}
						</div>
					</div>
				{/if}
			</div>

			<!-- Footer Actions -->
			<div class="sticky bottom-0 mt-4 p-4 border-t border-border/50 bg-background/98 backdrop-blur-xl shadow-lg">
				<div class="flex gap-2">
					<Button
						variant={user.enabled ? 'destructive' : 'default'}
						class="flex-1"
						onclick={() => onLockToggle(user?.id, !user?.enabled)}
						disabled={lockingUser}
					>
						{#if user.enabled}
							<Lock class="mr-2 h-4 w-4" />
							Lock User
						{:else}
							<Unlock class="mr-2 h-4 w-4" />
							Unlock User
						{/if}
					</Button>
					<Button 
						variant="outline" 
						onclick={() => onForcePasswordReset(user?.id, user?.email)}
						disabled={forcingPasswordReset}
					>
						<Key class="mr-2 h-4 w-4" />
						Reset Password
					</Button>
					<Button variant="outline" onclick={() => onOpenChange(false)}>
						Close
					</Button>
				</div>
			</div>
		{/if}
	</Sheet.Content>
</Sheet.Root>
