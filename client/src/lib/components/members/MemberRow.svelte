<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Badge } from '$lib/components/ui/badge';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import {
		Shield,
		Crown,
		MoreVertical,
		Trash2,
		ChevronDown,
		User as UserIcon
	} from '@lucide/svelte';
	import type { OrganizationMembershipResponse, OrganizationalRole } from '$lib/api/models';
	import { getInitials } from '$lib/utils/string';
	import { formatRelativeDate } from '$lib/utils/date';
	import RoleBadge from './RoleBadge.svelte';

	interface Props {
		member: OrganizationMembershipResponse;
		canManageMembers: boolean;
		isOwner: boolean;
		onUpdateRole: (member: OrganizationMembershipResponse, newRole: OrganizationalRole) => void;
		onRemove: (member: OrganizationMembershipResponse) => void;
		onTransferOwnership: (member: OrganizationMembershipResponse) => void;
	}

	let {
		member,
		canManageMembers,
		isOwner,
		onUpdateRole,
		onRemove,
		onTransferOwnership
	}: Props = $props();

	function getRoleBadgeClass(role: OrganizationalRole): string {
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
</script>

<div
	class="grid grid-cols-1 md:grid-cols-12 gap-2 md:gap-4 p-4 rounded-lg border border-border/50 bg-card/30 hover:bg-accent/5 transition-colors"
>
	<!-- Avatar & Name -->
	<div class="col-span-1 md:col-span-4 flex items-center gap-3">
		<div
			class="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 border border-primary/20 flex-shrink-0"
		>
		<span class="text-sm font-medium text-primary">
			{getInitials(member.userFirstName ?? '', member.userLastName ?? '')}
		</span>
		</div>
		<div class="min-w-0">
			<p class="font-medium truncate">
				{member.userFirstName}
				{member.userLastName}
			</p>
			<p class="text-sm text-muted-foreground md:hidden truncate">
				{member.userEmail}
			</p>
		</div>
	</div>

	<!-- Email (desktop only) -->
	<div class="hidden md:flex md:col-span-3 items-center">
		<p class="text-sm text-muted-foreground truncate">{member.userEmail}</p>
	</div>

	<!-- Role Badge/Selector -->
	<div class="col-span-1 md:col-span-2 flex items-center">
		{#if member.role === 'OWNER'}
			<RoleBadge role={member.role} />
		{:else if canManageMembers}
			<!-- Role dropdown for admins/owners -->
			<DropdownMenu.Root>
				<DropdownMenu.Trigger>
					{#snippet child({ props })}
						<Button
							{...props}
							variant="outline"
							size="sm"
							class="bg-background/50 border-border/50 hover:bg-accent/10"
						>
						<Badge class={getRoleBadgeClass(member.role ?? 'MEMBER')}>
							{#if member.role === 'ADMIN'}
								<Shield class="mr-1 h-3 w-3" />
							{/if}
							{member.role}
						</Badge>
							<ChevronDown class="ml-1 h-3 w-3" />
						</Button>
					{/snippet}
				</DropdownMenu.Trigger>
				<DropdownMenu.Content class="bg-card/95 backdrop-blur-xl border-border/50">
					<DropdownMenu.Item
						onclick={() => onUpdateRole(member, 'ADMIN')}
						class="hover:bg-accent/10"
					>
						<Shield class="mr-2 h-4 w-4" />
						ADMIN
					</DropdownMenu.Item>
					<DropdownMenu.Item
						onclick={() => onUpdateRole(member, 'MEMBER')}
						class="hover:bg-accent/10"
					>
						<UserIcon class="mr-2 h-4 w-4" />
						MEMBER
					</DropdownMenu.Item>
				</DropdownMenu.Content>
			</DropdownMenu.Root>
	{:else if member.role}
		<RoleBadge role={member.role} />
	{/if}
	</div>

	<!-- Joined Date -->
	<div class="col-span-1 md:col-span-2 flex items-center">
	<p class="text-sm text-muted-foreground">
		{formatRelativeDate(member.joinedAt ?? '')}
	</p>
	</div>

	<!-- Actions -->
	<div class="col-span-1 md:col-span-1 flex items-center justify-end">
		{#if canManageMembers && member.role !== 'OWNER'}
			<DropdownMenu.Root>
				<DropdownMenu.Trigger>
					{#snippet child({ props })}
						<Button
							{...props}
							variant="outline"
							size="sm"
							class="bg-background/50 border-border/50 hover:bg-accent/10"
						>
							<MoreVertical class="h-4 w-4" />
						</Button>
					{/snippet}
				</DropdownMenu.Trigger>
				<DropdownMenu.Content class="bg-card/95 backdrop-blur-xl border-border/50">
					{#if isOwner}
						<DropdownMenu.Item
							onclick={() => onTransferOwnership(member)}
							class="hover:bg-accent/10"
						>
							<Crown class="mr-2 h-4 w-4 text-primary" />
							Transfer Ownership
						</DropdownMenu.Item>
					{/if}
					<DropdownMenu.Item
						onclick={() => onRemove(member)}
						class="hover:bg-destructive/10 text-destructive"
					>
						<Trash2 class="mr-2 h-4 w-4" />
						Remove
					</DropdownMenu.Item>
				</DropdownMenu.Content>
			</DropdownMenu.Root>
		{/if}
	</div>
</div>
