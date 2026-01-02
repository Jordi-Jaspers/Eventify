<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Crown, AlertTriangle, LoaderCircle, ShieldAlert } from '@lucide/svelte';
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

	const confirmationText: string = 'Transfer Ownership';
	const isConfirmed: boolean = $derived(confirmation === confirmationText);

	function handleInput(event: Event): void {
		const target = event.target as HTMLInputElement;
		onConfirmationChange(target.value);
	}
</script>

<Sheet.Root {open} onOpenChange={onOpenChange}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6">
		<Sheet.Header>
			<Sheet.Title class="flex items-center gap-2 text-destructive">
				<ShieldAlert class="h-5 w-5" />
				Danger Zone: Transfer Ownership
			</Sheet.Title>
			<Sheet.Description class="text-sm">
				Transfer ownership of this organization to <strong class="text-foreground">{member?.userEmail}</strong>
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1 py-4 space-y-4">
			<!-- Danger Warning Box -->
			<div class="rounded-lg border-2 border-destructive/50 bg-destructive/10 p-4">
				<div class="flex gap-3">
					<AlertTriangle class="h-5 w-5 text-destructive shrink-0 mt-0.5" />
					<div class="space-y-2 text-sm">
						<p class="font-semibold text-destructive">This action is irreversible!</p>
						<ul class="text-muted-foreground space-y-1 list-disc list-inside">
							<li>You will be demoted to <strong class="text-foreground">ADMIN</strong></li>
							<li>You will lose all owner privileges</li>
							<li>Only the new owner or a global admin can reverse this</li>
						</ul>
					</div>
				</div>
			</div>

			<!-- Confirmation Input -->
			<div class="space-y-2">
				<Label class="text-sm">
					Type <code class="px-1.5 py-0.5 rounded bg-muted text-destructive font-mono text-xs">{confirmationText}</code> to confirm:
				</Label>
				<Input
					type="text"
					value={confirmation}
					oninput={handleInput}
					placeholder={`Type "${confirmationText}" to confirm`}
					disabled={transferring}
					class="bg-background/50 border-border focus:border-destructive focus:ring-destructive/20"
				/>
			</div>
		</div>

		<Sheet.Footer class="flex-row gap-3 pt-2 pb-2 border-t border-border/50">
			<Button
				variant="outline"
				onclick={() => onOpenChange(false)}
				disabled={transferring}
				class="flex-1"
			>
				Cancel
			</Button>
			<Button
				variant="destructive"
				onclick={onConfirm}
				disabled={transferring || !isConfirmed}
				class="flex-1"
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
