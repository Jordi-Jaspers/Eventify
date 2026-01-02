<script lang="ts">
	import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '$lib/components/ui/card';
	import { Users } from '@lucide/svelte';
	import type { OrganizationMembershipResponse, OrganizationalRole } from '$lib/api/models';
	import MemberRow from './MemberRow.svelte';

	interface Props {
		members: OrganizationMembershipResponse[];
		loading: boolean;
		canManageMembers: boolean;
		isOwner: boolean;
		onUpdateRole: (member: OrganizationMembershipResponse, newRole: OrganizationalRole) => void;
		onRemove: (member: OrganizationMembershipResponse) => void;
		onTransferOwnership: (member: OrganizationMembershipResponse) => void;
	}

	let {
		members,
		loading,
		canManageMembers,
		isOwner,
		onUpdateRole,
		onRemove,
		onTransferOwnership
	}: Props = $props();
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
	<CardHeader>
		<div class="flex items-center gap-2">
			<Users class="w-5 h-5 text-primary" />
			<CardTitle class="text-xl">Members</CardTitle>
		</div>
		<CardDescription>
			{members.length}
			{members.length === 1 ? 'member' : 'members'}
		</CardDescription>
	</CardHeader>
	<CardContent>
		{#if loading}
			<!-- Loading Skeleton -->
			<div class="space-y-3">
				{#each Array(5) as _, i}
					<div
						class="flex items-center gap-4 p-4 rounded-lg border border-border/50 bg-card/30"
					>
						<div class="h-10 w-10 rounded-full bg-muted/50 animate-pulse"></div>
						<div class="flex-1 space-y-2">
							<div class="h-4 bg-muted/50 rounded animate-pulse w-1/4"></div>
							<div class="h-3 bg-muted/50 rounded animate-pulse w-1/3"></div>
						</div>
						<div class="h-6 w-16 bg-muted/50 rounded animate-pulse"></div>
						<div class="h-4 w-24 bg-muted/50 rounded animate-pulse"></div>
					</div>
				{/each}
			</div>
		{:else if members.length === 0}
			<!-- Empty State -->
			<div class="flex flex-col items-center justify-center py-12">
				<div class="relative">
					<div
						class="absolute inset-0 blur-3xl bg-gradient-to-r from-primary/20 to-accent/20"
					></div>
					<div
						class="relative p-6 rounded-2xl bg-gradient-to-br from-primary/10 to-accent/10 border border-border/50 backdrop-blur-sm"
					>
						<Users class="w-12 h-12 text-primary" />
					</div>
				</div>
				<h3 class="mt-6 text-lg font-semibold">No members yet</h3>
				<p class="mt-2 text-sm text-muted-foreground text-center max-w-sm">
					Add members to collaborate on this organization
				</p>
			</div>
		{:else}
			<!-- Members List -->
			<div class="space-y-2">
				<!-- Desktop Table Header -->
				<div
					class="hidden md:grid md:grid-cols-12 gap-4 px-4 py-2 text-sm font-medium text-muted-foreground border-b border-border/50"
				>
					<div class="col-span-4">Member</div>
					<div class="col-span-3">Email</div>
					<div class="col-span-2">Role</div>
					<div class="col-span-2">Joined</div>
					<div class="col-span-1 text-right">Actions</div>
				</div>

				<!-- Member Rows -->
				{#each members as member (member.id)}
					<MemberRow
						{member}
						{canManageMembers}
						{isOwner}
						{onUpdateRole}
						{onRemove}
						{onTransferOwnership}
					/>
				{/each}
			</div>
		{/if}
	</CardContent>
</Card>
