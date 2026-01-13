<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Label } from '$lib/components/ui/label';
	import { Textarea } from '$lib/components/ui/textarea';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Radio, LoaderCircle, Tag, FileText } from '@lucide/svelte';

	interface Props {
		open: boolean;
		creating: boolean;
		onOpenChange: (open: boolean) => void;
		onSubmit: (name: string, description: string | undefined) => void;
	}

	let { open, creating, onOpenChange, onSubmit }: Props = $props();

	let name: string = $state('');
	let description: string = $state('');

	function handleSubmit(): void {
		if (!name.trim()) return;
		onSubmit(name.trim(), description.trim() || undefined);
		// Reset form
		name = '';
		description = '';
	}

	function handleOpenChange(newOpen: boolean): void {
		onOpenChange(newOpen);
		if (!newOpen) {
			// Reset form when closing
			name = '';
			description = '';
		}
	}

	const canSubmit: boolean = $derived(name.trim().length > 0 && !creating);
</script>

<Sheet.Root {open} onOpenChange={handleOpenChange}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6 sm:max-w-lg">
		<Sheet.Header class="pt-6">
			<Sheet.Title class="flex items-center gap-2 text-lg">
				<div
					class="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary/20 to-accent/20 border border-primary/30"
				>
					<Radio class="h-4 w-4 text-primary" />
				</div>
				Create Channel
			</Sheet.Title>
			<Sheet.Description class="text-sm">
				Create a new channel for organizing your events and notifications.
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1 py-6 space-y-6">
			<!-- Name Input Card -->
			<div
				class="rounded-xl border border-border/50 bg-background/30 backdrop-blur-sm p-4 space-y-3"
			>
				<div class="flex items-center gap-2">
					<Tag class="h-4 w-4 text-primary" />
					<Label for="channel-name" class="text-sm font-medium">Channel Name</Label>
					<span class="text-destructive text-sm">*</span>
				</div>
				<input
					id="channel-name"
					type="text"
					bind:value={name}
					placeholder="e.g., Production Events, User Notifications"
					disabled={creating}
					class="flex h-10 w-full rounded-lg border border-border/50 bg-background/50 px-3 text-sm shadow-sm transition-all placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/30 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
				/>
				<p class="text-xs text-muted-foreground">
					Use a descriptive name to identify this channel's purpose
				</p>
			</div>

			<!-- Description Textarea Card -->
			<div
				class="rounded-xl border border-border/50 bg-background/30 backdrop-blur-sm p-4 space-y-3"
			>
				<div class="flex items-center gap-2">
					<FileText class="h-4 w-4 text-primary" />
					<Label for="channel-description" class="text-sm font-medium">Description</Label>
					<span class="text-muted-foreground text-xs">(Optional)</span>
				</div>
				<Textarea
					id="channel-description"
					bind:value={description}
					placeholder="Describe the purpose and use case of this channel..."
					disabled={creating}
					rows={4}
					class="resize-none bg-background/50 border-border/50 focus-visible:ring-2 focus-visible:ring-primary/30 focus-visible:border-primary"
				/>
				<p class="text-xs text-muted-foreground">
					Add additional context about this channel (optional)
				</p>
			</div>
		</div>

		<Sheet.Footer class="flex-row gap-3 sm:flex-row pb-6">
			<Button
				variant="outline"
				onclick={() => handleOpenChange(false)}
				disabled={creating}
				class="flex-1 bg-background/50 border-border/50 hover:bg-background/70"
			>
				Cancel
			</Button>
			<Button
				onclick={handleSubmit}
				disabled={!canSubmit}
				class="flex-1 bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 shadow-lg shadow-primary/20 transition-all"
			>
				{#if creating}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Creating...
				{:else}
					<Radio class="mr-2 h-4 w-4" />
					Create Channel
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
