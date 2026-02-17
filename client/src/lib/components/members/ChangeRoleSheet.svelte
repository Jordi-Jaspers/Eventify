<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import * as Sheet from '$lib/components/ui/sheet';
	import Label from '$lib/components/ui/label/label.svelte';
	import { LoaderCircle, Shield, User as UserIcon, Check } from '@lucide/svelte';
	import type { OrganizationMembershipResponse, OrganizationalRole } from '$lib/api/models';
	import { getInitials } from '$lib/utils/string';

	interface Props {
		open: boolean;
		member: OrganizationMembershipResponse | null;
		selectedRole: OrganizationalRole;
		changing: boolean;
		onOpenChange: (open: boolean) => void;
		onRoleChange: (role: OrganizationalRole) => void;
		onConfirm: () => void;
	}

	let { open, member, selectedRole, changing, onOpenChange, onRoleChange, onConfirm }: Props = $props();

	const roles: { value: OrganizationalRole; label: string; description: string; icon: typeof Shield }[] = [
		{
			value: 'ADMIN',
			label: 'Admin',
			description: 'Can manage members, channels, and organization settings',
			icon: Shield
		},
		{
			value: 'MEMBER',
			label: 'Member',
			description: 'Can view organization resources and use channels',
			icon: UserIcon
		}
	];
</script>

<Sheet.Root {open} onOpenChange={onOpenChange}>
	<Sheet.Content class="w-full sm:max-w-md bg-background/95 backdrop-blur-xl border-border/50">
		<Sheet.Header>
			<Sheet.Title>Change Role</Sheet.Title>
			<Sheet.Description>
				Update the role for this organization member.
			</Sheet.Description>
		</Sheet.Header>

		{#if member}
			<div class="py-6 space-y-6">
				<!-- Member Info -->
				<div class="flex items-center gap-3 p-4 rounded-lg bg-muted/30 border border-border/50">
					<div class="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 border border-primary/20">
						<span class="text-sm font-medium text-primary">
							{getInitials(member.userFirstName ?? '', member.userLastName ?? '')}
						</span>
					</div>
					<div>
						<p class="font-medium">
							{member.userFirstName ?? ''} {member.userLastName ?? ''}
						</p>
						<p class="text-sm text-muted-foreground">{member.userEmail ?? ''}</p>
					</div>
				</div>

				<!-- Role Selection -->
				<div class="space-y-3">
					<Label class="text-sm font-medium">Select Role</Label>
					<div class="space-y-3">
						{#each roles as role}
							{@const isSelected = selectedRole === role.value}
							<button
								type="button"
								class="w-full flex items-start gap-3 p-4 rounded-lg border transition-colors text-left
									{isSelected 
										? 'border-primary bg-primary/5' 
										: 'border-border/50 bg-card/50 hover:bg-muted/30'}"
								onclick={() => onRoleChange(role.value)}
							>
								<div class="flex h-5 w-5 items-center justify-center rounded-full border-2 mt-0.5 flex-shrink-0
									{isSelected ? 'border-primary bg-primary' : 'border-muted-foreground/50'}">
									{#if isSelected}
										<Check class="h-3 w-3 text-primary-foreground" />
									{/if}
								</div>
								<div class="flex-1">
									<div class="flex items-center gap-2">
										<role.icon class="h-4 w-4 {isSelected ? 'text-primary' : 'text-muted-foreground'}" />
										<span class="font-medium">{role.label}</span>
									</div>
									<p class="text-sm text-muted-foreground mt-1">{role.description}</p>
								</div>
							</button>
						{/each}
					</div>
				</div>
			</div>

			<Sheet.Footer>
				<Button variant="outline" onclick={() => onOpenChange(false)} disabled={changing}>
					Cancel
				</Button>
				<Button 
					onclick={onConfirm} 
					disabled={changing || selectedRole === member.role}
				>
					{#if changing}
						<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					{/if}
					Save Changes
				</Button>
			</Sheet.Footer>
		{/if}
	</Sheet.Content>
</Sheet.Root>
