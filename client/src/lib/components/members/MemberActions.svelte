<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { MoreVertical, Crown, Trash2, UserCog } from '@lucide/svelte';
	import type { OrganizationMembershipResponse } from '$lib/api/models';

	interface Props {
		member: OrganizationMembershipResponse;
		canManage: boolean;
		isOwner: boolean;
		onChangeRole: (member: OrganizationMembershipResponse) => void;
		onTransferOwnership: (member: OrganizationMembershipResponse) => void;
		onRemove: (member: OrganizationMembershipResponse) => void;
	}

	let { member, canManage, isOwner, onChangeRole, onTransferOwnership, onRemove }: Props = $props();
</script>

{#if canManage && member.role !== 'OWNER'}
	<div class="flex items-center justify-end">
		<DropdownMenu.Root>
			<DropdownMenu.Trigger>
				<Button
					variant="ghost"
					size="icon"
					class="h-8 w-8 text-muted-foreground hover:text-primary"
					aria-label="Member actions"
				>
					<MoreVertical class="h-4 w-4" />
				</Button>
			</DropdownMenu.Trigger>
			<DropdownMenu.Content align="end" class="w-48 bg-card/95 backdrop-blur-xl border-border/50">
				<DropdownMenu.Item onclick={() => onChangeRole(member)} class="cursor-pointer">
					<UserCog class="mr-2 h-4 w-4" />
					Change Role
				</DropdownMenu.Item>
				{#if isOwner}
					<DropdownMenu.Separator />
					<DropdownMenu.Item onclick={() => onTransferOwnership(member)} class="cursor-pointer">
						<Crown class="mr-2 h-4 w-4 text-primary" />
						Transfer Ownership
					</DropdownMenu.Item>
				{/if}
				<DropdownMenu.Separator />
				<DropdownMenu.Item onclick={() => onRemove(member)} class="cursor-pointer text-destructive">
					<Trash2 class="mr-2 h-4 w-4" />
					Remove
				</DropdownMenu.Item>
			</DropdownMenu.Content>
		</DropdownMenu.Root>
	</div>
{/if}
