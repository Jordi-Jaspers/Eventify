<script lang="ts">
	import * as Sheet from '$lib/components/ui/sheet';
	import * as Select from '$lib/components/ui/select';
	import { Label } from '$lib/components/ui/label';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import { Building2, CircleAlert, LoaderCircle, Activity } from '@lucide/svelte';
	import { updateOrganizationStatus } from '$lib/api/admin/AdminOrganizationController';
	import type { OrganizationResponse, OrganizationStatus } from '$lib/api/models';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';

	interface Props {
		open: boolean;
		onOpenChange: (open: boolean) => void;
		onSuccess: () => void;
		organization: OrganizationResponse | null;
	}

	let { open, onOpenChange, onSuccess, organization }: Props = $props();

	let selectedStatus: OrganizationStatus = $state('ACTIVE');
	let isSubmitting: boolean = $state(false);
	let errorMessage: string = $state('');

	$effect(() => {
		if (organization?.status) {
			selectedStatus = organization.status;
		}
	});

	async function handleSubmit(): Promise<void> {
		if (!organization?.id) return;

		isSubmitting = true;
		errorMessage = '';

		try {
			const updated: OrganizationResponse = await updateOrganizationStatus(organization.id, selectedStatus);
			toast.success(`Organization "${updated.name}" status updated to ${updated.status}`);
			onSuccess();
			onOpenChange(false);
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to update organization status');
			errorMessage = message;
			toast.error(message);
		} finally {
			isSubmitting = false;
		}
	}

	function handleOpenChange(newOpen: boolean): void {
		onOpenChange(newOpen);
		if (!newOpen) {
			errorMessage = '';
		}
	}

	const canSubmit: boolean = $derived(
		!!organization && selectedStatus !== organization.status && !isSubmitting
	);

	const statusOptions: { value: OrganizationStatus; label: string }[] = [
		{ value: 'ACTIVE', label: 'Active' },
		{ value: 'SUSPENDED', label: 'Suspended' }
	];
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
				Edit Organization
			</Sheet.Title>
			<Sheet.Description class="text-sm">
				Update the status for <strong>{organization?.name ?? 'this organization'}</strong>.
			</Sheet.Description>
		</Sheet.Header>

		{#if errorMessage}
			<Alert variant="destructive" class="mt-4 bg-destructive/10 border-destructive/30">
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>{errorMessage}</AlertDescription>
			</Alert>
		{/if}

		<div class="flex-1 py-6 space-y-6">
		<div class="rounded-xl border border-border/50 bg-background/30 backdrop-blur-sm p-4 space-y-3">
				<div class="flex items-center gap-2">
					<Activity class="h-4 w-4 text-primary" />
					<Label class="text-sm font-medium">Organization Status</Label>
					<span class="text-destructive text-sm">*</span>
				</div>
				<Select.Root
					type="single"
					value={selectedStatus}
					onValueChange={(value: string) => { selectedStatus = value as OrganizationStatus; }}
					disabled={isSubmitting}
				>
					<Select.Trigger class="w-full bg-background/50 border-border/50">
						{selectedStatus}
					</Select.Trigger>
					<Select.Content class="bg-card/95 backdrop-blur-xl border-border/50">
						{#each statusOptions as option (option.value)}
							<Select.Item value={option.value} label={option.label}>
								{option.label}
							</Select.Item>
						{/each}
					</Select.Content>
				</Select.Root>
				<p class="text-xs text-muted-foreground">
					Suspended organizations will be inaccessible to their members.
				</p>
			</div>

			<div class="rounded-xl border border-primary/20 bg-primary/5 backdrop-blur-sm p-4">
				<p class="text-sm text-muted-foreground">
					<strong class="text-foreground">Note:</strong> Changing the status to SUSPENDED will
					prevent all members from accessing this organization.
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
					Saving...
				{:else}
					<Building2 class="mr-2 h-4 w-4" />
					Save Changes
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
