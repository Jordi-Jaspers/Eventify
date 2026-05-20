<script lang="ts">
	import * as Sheet from '$lib/components/ui/sheet';
	import { Label } from '$lib/components/ui/label';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import UserSearchCombobox from '$lib/components/user/UserSearchCombobox.svelte';
	import { Building2, CircleAlert, LoaderCircle, Tag, UserCircle } from '@lucide/svelte';
	import { createOrganization } from '$lib/api/admin/AdminOrganizationController';
	import type { OrganizationResponse, UserResponse } from '$lib/api/models';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';

	interface Props {
		open: boolean;
		onOpenChange: (open: boolean) => void;
		onSuccess: () => void;
	}

	let { open, onOpenChange, onSuccess }: Props = $props();

	let organizationName: string = $state('');
	let selectedOwner: UserResponse | undefined = $state(undefined);
	let isSubmitting: boolean = $state(false);
	let errors: Record<string, string> = $state({});

	function validateForm(): boolean {
		errors = {};

		const trimmedName: string = organizationName.trim();

		if (!trimmedName) {
			errors.name = 'Organization name is required';
			return false;
		}

		if (trimmedName.length < 3) {
			errors.name = 'Organization name must be at least 3 characters';
			return false;
		}

		if (trimmedName.length > 100) {
			errors.name = 'Organization name must not exceed 100 characters';
			return false;
		}

		if (!selectedOwner) {
			errors.owner = 'Please select an organization owner';
			return false;
		}

		return true;
	}

	async function handleSubmit(): Promise<void> {
		if (!validateForm()) {
			return;
		}

		isSubmitting = true;
		errors = {};

		try {
			if (!selectedOwner || !selectedOwner.email) {
				throw new Error('Owner must be selected');
			}

			const ownerEmail: string = selectedOwner.email;
			const response: OrganizationResponse = await createOrganization(
				organizationName.trim(),
				ownerEmail
			);
			toast.success(`Organization "${response.name}" created successfully`);

			// Reset form and close
			resetForm();
			onSuccess();
			onOpenChange(false);
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to create organization');
			errors.general = message;
			toast.error(message);
		} finally {
			isSubmitting = false;
		}
	}

	function resetForm(): void {
		organizationName = '';
		selectedOwner = undefined;
		errors = {};
	}

	function handleOpenChange(newOpen: boolean): void {
		onOpenChange(newOpen);
		if (!newOpen) {
			resetForm();
		}
	}

	const canSubmit: boolean = $derived(
		organizationName.trim().length >= 3 && selectedOwner !== undefined && !isSubmitting
	);
</script>

<Sheet.Root {open} onOpenChange={handleOpenChange}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6 sm:max-w-lg">
		<Sheet.Header class="pt-6">
			<Sheet.Title class="flex items-center gap-2 text-lg">
				<div
					class="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary/20 to-accent/20 border border-primary/30"
				>
					<Building2 class="h-4 w-4 text-primary" />
				</div>
				Create Organization
			</Sheet.Title>
			<Sheet.Description class="text-sm">
				Provision a new organization for multi-tenant management.
			</Sheet.Description>
		</Sheet.Header>

		<!-- General Error Alert -->
		{#if errors.general}
			<Alert variant="destructive" class="mt-4 bg-destructive/10 border-destructive/30">
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>{errors.general}</AlertDescription>
			</Alert>
		{/if}

		<div class="flex-1 py-6 space-y-6">
			<!-- Organization Name Input Card -->
			<div class="rounded-xl border border-border/50 bg-background/30 backdrop-blur-sm p-4 space-y-3">
				<div class="flex items-center gap-2">
					<Tag class="h-4 w-4 text-primary" />
					<Label for="organization-name" class="text-sm font-medium">Organization Name</Label>
					<span class="text-destructive text-sm">*</span>
				</div>
				<input
					id="organization-name"
					type="text"
					bind:value={organizationName}
					placeholder="e.g., Acme Corporation"
					disabled={isSubmitting}
					class="flex h-10 w-full rounded-lg border border-border/50 bg-background/50 px-3 text-sm shadow-sm transition-all placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/30 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
				/>
				{#if errors.name}
					<p class="text-sm text-destructive">{errors.name}</p>
				{:else}
					<p class="text-xs text-muted-foreground">
						Must be between 3 and 100 characters
					</p>
				{/if}
			</div>

			<!-- Owner Selection Card -->
			<div class="rounded-xl border border-border/50 bg-background/30 backdrop-blur-sm p-4 space-y-3">
				<div class="flex items-center gap-2">
					<UserCircle class="h-4 w-4 text-primary" />
					<Label for="owner-search" class="text-sm font-medium">Organization Owner</Label>
					<span class="text-destructive text-sm">*</span>
				</div>
				<UserSearchCombobox
					onSelect={(user: UserResponse) => {
						selectedOwner = user;
					}}
					selectedUser={selectedOwner}
					placeholder="Search for organization owner..."
					disabled={isSubmitting}
				/>
				{#if errors.owner}
					<p class="text-sm text-destructive">{errors.owner}</p>
				{:else}
					<p class="text-xs text-muted-foreground">
						The owner will have full control of the organization
					</p>
				{/if}
			</div>

			<!-- Info Box -->
			<div class="rounded-xl border border-primary/20 bg-primary/5 backdrop-blur-sm p-4">
				<p class="text-sm text-muted-foreground">
					<strong class="text-foreground">Note:</strong> Creating an organization will generate a
					unique slug and set the status to ACTIVE. You can manage organization settings after
					creation.
				</p>
			</div>
		</div>

		<Sheet.Footer class="flex-row gap-3 sm:flex-row pb-6">
			<Button
				variant="outline"
				onclick={() => handleOpenChange(false)}
				disabled={isSubmitting}
				class="flex-1 bg-background/50 border-border/50 hover:bg-background/70"
			>
				Cancel
			</Button>
			<Button onclick={handleSubmit} disabled={!canSubmit} class="flex-1">
				{#if isSubmitting}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Creating...
				{:else}
					<Building2 class="mr-2 h-4 w-4" />
					Create Organization
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
