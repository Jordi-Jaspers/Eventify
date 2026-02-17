<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import * as Sheet from '$lib/components/ui/sheet';
	import {
		LoaderCircle,
		Shield,
		User as UserIcon,
		Check,
		Users,
		Radio,
		Settings,
		Eye
	} from '@lucide/svelte';
	import type { OrganizationMembershipResponse, OrganizationalRole } from '$lib/api/models';
	import { getInitials } from '$lib/utils/string';
	import RoleBadge from './RoleBadge.svelte';

	interface Props {
		open: boolean;
		member: OrganizationMembershipResponse | null;
		selectedRole: OrganizationalRole;
		changing: boolean;
		onOpenChange: (open: boolean) => void;
		onRoleChange: (role: OrganizationalRole) => void;
		onConfirm: () => void;
	}

	let { open, member, selectedRole, changing, onOpenChange, onRoleChange, onConfirm }: Props =
		$props();

	const roles: {
		value: OrganizationalRole;
		label: string;
		description: string;
		icon: typeof Shield;
		permissions: string[];
	}[] = [
		{
			value: 'ADMIN',
			label: 'Administrator',
			description: 'Full management access to the organization',
			icon: Shield,
			permissions: [
				'Manage organization settings',
				'Invite and remove members',
				'Create and manage channels',
				'View all events and analytics'
			]
		},
		{
			value: 'MEMBER',
			label: 'Member',
			description: 'Standard access to organization resources',
			icon: UserIcon,
			permissions: [
				'View organization resources',
				'Access assigned channels',
				'View events on channels',
				'Use API with limited scope'
			]
		}
	];

	const hasChanges = $derived(member && selectedRole !== member.role);
</script>

<Sheet.Root {open} onOpenChange={onOpenChange}>
	<Sheet.Content
		class="w-full sm:max-w-md bg-background/95 backdrop-blur-xl border-border/50 overflow-y-auto p-0"
	>
		{#if member}
			<!-- Hero Header with Gradient -->
			<div
				class="relative bg-gradient-to-br from-primary/20 via-accent/15 to-background/50 pt-8 pb-12 px-6 backdrop-blur-sm"
			>
				<div class="absolute inset-0 bg-grid-white/5"></div>
				<div class="relative flex flex-col items-center text-center">
					<!-- Member Avatar -->
					<div
						class="h-20 w-20 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center shadow-lg shadow-primary/25 ring-4 ring-background"
					>
						<span class="text-2xl font-semibold text-white">
							{getInitials(member.userFirstName ?? '', member.userLastName ?? '')}
						</span>
					</div>

					<!-- Member Name -->
					<h2 class="mt-4 text-xl font-semibold">
						{member.userFirstName ?? ''}
						{member.userLastName ?? ''}
					</h2>

					<!-- Email -->
					<p class="mt-1 text-sm text-muted-foreground">{member.userEmail ?? ''}</p>

					<!-- Current Role Badge -->
					<div class="mt-3 flex items-center gap-2">
						<span class="text-xs text-muted-foreground">Current role:</span>
						<RoleBadge role={member.role!} />
					</div>
				</div>
			</div>

			<!-- Content -->
			<div class="px-6 py-4 -mt-6 space-y-4">
				<!-- Role Selection Section -->
				<div class="rounded-xl border border-border/50 bg-card/50 backdrop-blur-sm p-4 shadow-sm">
					<p class="text-xs text-muted-foreground uppercase tracking-wide mb-4">Select New Role</p>

					<div class="space-y-3">
						{#each roles as role}
							{@const isSelected = selectedRole === role.value}
							{@const isCurrent = member.role === role.value}
							<button
								type="button"
								class="w-full rounded-xl border-2 p-4 text-left transition-all duration-200
									{isSelected
									? 'border-primary bg-primary/5 shadow-md shadow-primary/10'
									: 'border-border/50 bg-card/30 hover:border-border hover:bg-card/50'}"
								onclick={() => onRoleChange(role.value)}
							>
								<div class="flex items-start gap-4">
									<!-- Role Icon -->
									<div
										class="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl transition-colors
											{isSelected
											? 'bg-primary text-primary-foreground'
											: 'bg-muted/50 text-muted-foreground'}"
									>
										<role.icon class="h-6 w-6" />
									</div>

									<!-- Role Info -->
									<div class="flex-1 min-w-0">
										<div class="flex items-center gap-2">
											<span class="font-semibold {isSelected ? 'text-primary' : ''}"
												>{role.label}</span
											>
											{#if isCurrent}
												<span
													class="text-[10px] uppercase tracking-wider px-1.5 py-0.5 rounded bg-muted text-muted-foreground"
												>
													Current
												</span>
											{/if}
										</div>
										<p class="text-sm text-muted-foreground mt-0.5">{role.description}</p>
									</div>

									<!-- Selection Indicator -->
									<div
										class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full border-2 transition-all
											{isSelected
											? 'border-primary bg-primary'
											: 'border-muted-foreground/30'}"
									>
										{#if isSelected}
											<Check class="h-3.5 w-3.5 text-primary-foreground" />
										{/if}
									</div>
								</div>

								<!-- Permissions List -->
								<div class="mt-4 pt-3 border-t border-border/50">
									<p class="text-[10px] uppercase tracking-wider text-muted-foreground mb-2">
										Permissions
									</p>
									<ul class="grid grid-cols-1 gap-1.5">
										{#each role.permissions as permission}
											<li class="flex items-center gap-2 text-xs text-muted-foreground">
												<Check
													class="h-3 w-3 shrink-0 {isSelected
														? 'text-primary'
														: 'text-muted-foreground/50'}"
												/>
												<span>{permission}</span>
											</li>
										{/each}
									</ul>
								</div>
							</button>
						{/each}
					</div>
				</div>

				<!-- Change Summary -->
				{#if hasChanges}
					<div
						class="rounded-xl border border-amber-500/30 bg-amber-500/5 p-4 flex items-start gap-3"
					>
						<div
							class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-amber-500/10"
						>
							<Shield class="h-4 w-4 text-amber-500" />
						</div>
						<div>
							<p class="text-sm font-medium text-amber-500">Role Change</p>
							<p class="text-xs text-muted-foreground mt-0.5">
								{member.userFirstName}'s role will change from
								<span class="font-medium">{member.role}</span>
								to
								<span class="font-medium">{selectedRole}</span>
							</p>
						</div>
					</div>
				{/if}
			</div>

			<!-- Footer Actions -->
			<div
				class="sticky bottom-0 mt-4 p-4 border-t border-border/50 bg-background/98 backdrop-blur-xl shadow-lg"
			>
				<div class="flex gap-2">
					<Button variant="outline" class="flex-1" onclick={() => onOpenChange(false)} disabled={changing}>
						Cancel
					</Button>
					<Button class="flex-1" onclick={onConfirm} disabled={changing || !hasChanges}>
						{#if changing}
							<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
						{/if}
						Save Changes
					</Button>
				</div>
			</div>
		{/if}
	</Sheet.Content>
</Sheet.Root>
