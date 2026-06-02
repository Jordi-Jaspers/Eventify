<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Label } from '$lib/components/ui/label';
	import * as Tooltip from '$lib/components/ui/tooltip';
	import { Shield, User as UserIcon, Crown } from '@lucide/svelte';
	import type { OrganizationalRole } from '$lib/api/models';

	interface Props {
		selectedRole: OrganizationalRole;
		adding: boolean;
		hasOwner: boolean;
		isGlobalAdmin: boolean;
		onRoleChange: (role: OrganizationalRole) => void;
	}

	let { selectedRole, adding, hasOwner, isGlobalAdmin, onRoleChange }: Props = $props();

	const ownerButtonDisabled: boolean = $derived(hasOwner || !isGlobalAdmin);
	const ownerButtonTooltip: string = $derived(
		hasOwner
			? 'This organization already has an owner'
			: !isGlobalAdmin
				? 'Only global admins can assign owners'
				: ''
	);
</script>

<div class="space-y-2">
	<Label>Role</Label>
	<div class="flex gap-2">
		{#if ownerButtonDisabled}
			<Tooltip.Provider>
				<Tooltip.Root>
					<Tooltip.Trigger>
						{#snippet child({ props })}
							<Button
								{...props}
								variant="outline"
								size="sm"
								disabled={true}
								class="bg-background/50 border-border/50 opacity-50 cursor-not-allowed"
							>
								<Crown class="mr-2 h-4 w-4" />
								OWNER
							</Button>
						{/snippet}
					</Tooltip.Trigger>
					<Tooltip.Content>
						<p>{ownerButtonTooltip}</p>
					</Tooltip.Content>
				</Tooltip.Root>
			</Tooltip.Provider>
		{:else}
			<Button
				variant={selectedRole === 'OWNER' ? 'default' : 'outline'}
				size="sm"
				onclick={() => onRoleChange('OWNER')}
				disabled={adding}
				class={selectedRole !== 'OWNER' ? 'bg-background/50 border-border/50' : ''}
			>
				<Crown class="mr-2 h-4 w-4" />
				OWNER
			</Button>
		{/if}
		<Button
			variant={selectedRole === 'ADMIN' ? 'default' : 'outline'}
			size="sm"
			onclick={() => onRoleChange('ADMIN')}
			disabled={adding}
			class={selectedRole !== 'ADMIN' ? 'bg-background/50 border-border/50' : ''}
		>
			<Shield class="mr-2 h-4 w-4" />
			ADMIN
		</Button>
		<Button
			variant={selectedRole === 'MEMBER' ? 'default' : 'outline'}
			size="sm"
			onclick={() => onRoleChange('MEMBER')}
			disabled={adding}
			class={selectedRole !== 'MEMBER' ? 'bg-background/50 border-border/50' : ''}
		>
			<UserIcon class="mr-2 h-4 w-4" />
			MEMBER
		</Button>
	</div>
</div>
