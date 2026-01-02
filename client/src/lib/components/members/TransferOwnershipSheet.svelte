<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Crown, CircleAlert, LoaderCircle } from '@lucide/svelte';
	import type { OrganizationMembershipResponse } from '$lib/api/models';

	interface Props {
		open: boolean;
		member: OrganizationMembershipResponse | null;
		confirmation: string;
		transferring: boolean;
		onOpenChange: (open: boolean) => void;
		onConfirmationChange: (value: string) => void;
		onConfirm: () => void;
	}

	let {
		open,
		member,
		confirmation,
		transferring,
		onOpenChange,
		onConfirmationChange,
		onConfirm
	}: Props = $props();

	function handleInput(event: Event): void {
		const target = event.target as HTMLInputElement;
		onConfirmationChange(target.value);
	}
</script>

<Sheet.Root {open} onOpenChange={onOpenChange}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6">
		<Sheet.Header class="pt-6">
			<Sheet.Title class="flex items-center gap-2">
				<Crown class="h-5 w-5 text-primary" />
				Transfer Ownership
			</Sheet.Title>
			<Sheet.Description>
				You are about to transfer ownership to <strong>{member?.userEmail}</strong>.
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1 space-y-4 py-6">
			<Alert class="bg-destructive/10 border-destructive/50">
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>
					<strong>Warning:</strong> After this transfer, you will become an ADMIN and will no longer
					have owner privileges. This action cannot be undone.
				</AlertDescription>
			</Alert>

			<div class="space-y-2">
				<Label>Type <strong>Transfer Ownership</strong> to confirm:</Label>
				<Input
					type="text"
					value={confirmation}
					oninput={handleInput}
					placeholder="Type 'Transfer Ownership' to confirm"
					disabled={transferring}
					class="bg-background/50 border-border"
				/>
			</div>
		</div>

		<Sheet.Footer class="flex-row gap-2 sm:flex-row pb-6">
			<Button
				variant="outline"
				onclick={() => onOpenChange(false)}
				disabled={transferring}
				class="flex-1 bg-background/50 border-border/50"
			>
				Cancel
			</Button>
			<Button
				onclick={onConfirm}
				disabled={transferring || confirmation !== 'Transfer Ownership'}
				class="flex-1 bg-gradient-to-r from-primary to-accent hover:opacity-90"
			>
				{#if transferring}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Transferring...
				{:else}
					<Crown class="mr-2 h-4 w-4" />
					Transfer Ownership
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
