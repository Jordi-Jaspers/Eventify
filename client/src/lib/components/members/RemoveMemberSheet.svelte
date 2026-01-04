<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Trash2, LoaderCircle } from '@lucide/svelte';
	import type { OrganizationMembershipResponse } from '$lib/api/models';

	interface Props {
		open: boolean;
		member: OrganizationMembershipResponse | null;
		removing: boolean;
		onOpenChange: (open: boolean) => void;
		onConfirm: () => void;
	}

	let { open, member, removing, onOpenChange, onConfirm }: Props = $props();
</script>

<Sheet.Root {open} onOpenChange={onOpenChange}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6">
		<Sheet.Header class="pt-6">
			<Sheet.Title>Remove Member</Sheet.Title>
			<Sheet.Description>
				Are you sure you want to remove <strong>{member?.userEmail}</strong> from this organization?
				This action cannot be undone.
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1"></div>

		<Sheet.Footer class="flex-row gap-2 sm:flex-row pb-6">
			<Button
				variant="outline"
				onclick={() => onOpenChange(false)}
				disabled={removing}
				class="flex-1 bg-background/50 border-border/50"
			>
				Cancel
			</Button>
			<Button
				onclick={onConfirm}
				disabled={removing}
				class="flex-1 bg-destructive hover:bg-destructive/90"
			>
				{#if removing}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Removing...
				{:else}
					<Trash2 class="mr-2 h-4 w-4" />
					Remove
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
