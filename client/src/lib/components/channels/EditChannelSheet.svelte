<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Label } from '$lib/components/ui/label';
	import { Textarea } from '$lib/components/ui/textarea';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Radio, LoaderCircle, Hash, FileText } from '@lucide/svelte';
	import type { ChannelDetailsResponse } from '$lib/api/models';

	interface Props {
		open: boolean;
		channel: ChannelDetailsResponse | null;
		updating: boolean;
		onOpenChange: (open: boolean) => void;
		onSubmit: (channelId: number, name: string, description: string | undefined) => void;
	}

	let { open, channel, updating, onOpenChange, onSubmit }: Props = $props();

	let name: string = $state('');
	let description: string = $state('');

	// Update form when channel changes
	$effect(() => {
		if (channel) {
			name = channel.name || '';
			description = channel.description || '';
		}
	});

	function handleSubmit(): void {
		if (!name.trim() || !channel) return;
		onSubmit(channel.id || 0, name.trim(), description.trim() || undefined);
	}

	function handleOpenChange(newOpen: boolean): void {
		onOpenChange(newOpen);
		if (!newOpen) {
			// Reset form when closing
			name = '';
			description = '';
		}
	}

	const canSubmit: boolean = $derived(name.trim().length > 0 && !updating);
</script>

<Sheet.Root {open} onOpenChange={handleOpenChange}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6 sm:max-w-md">
		<Sheet.Header class="pt-6">
			<Sheet.Title class="flex items-center gap-3 text-lg">
				<div class="flex h-9 w-9 items-center justify-center rounded-md bg-primary/10 border border-primary/20">
					<Radio class="h-4 w-4 text-primary" />
				</div>
				Edit Channel
			</Sheet.Title>
			<Sheet.Description class="text-sm text-muted-foreground">
				Update the name and description of your channel.
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1 py-6 space-y-4">
			<!-- Channel Name & Slug Card -->
			<div class="rounded-md border border-border/50 bg-muted/20 p-4 space-y-4">
				<!-- Channel Name -->
				<div class="space-y-2">
					<Label for="edit-channel-name" class="text-sm font-medium flex items-center gap-2">
						<Radio class="h-3.5 w-3.5 text-primary" />
						Channel Name <span class="text-destructive">*</span>
					</Label>
					<input
						id="edit-channel-name"
						type="text"
						bind:value={name}
						placeholder="e.g., Production Events"
						disabled={updating}
						class="flex h-9 w-full rounded-md border border-border/50 bg-background/80 px-3 text-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
					/>
				</div>

				<!-- Slug (Read-only) -->
				<div class="space-y-2">
					<Label for="edit-channel-slug" class="text-sm font-medium flex items-center gap-2">
						<Hash class="h-3.5 w-3.5 text-muted-foreground" />
						Slug
					</Label>
					<input
						id="edit-channel-slug"
						type="text"
						value={channel?.slug || ''}
						disabled
						class="flex h-9 w-full rounded-md border border-border/30 bg-muted/30 px-3 text-sm font-mono cursor-not-allowed opacity-60"
					/>
					<p class="text-xs text-muted-foreground">
						The slug cannot be changed after creation
					</p>
				</div>
			</div>

			<!-- Description Card -->
			<div class="rounded-md border border-border/50 bg-muted/20 p-4 space-y-2">
				<Label for="edit-channel-description" class="text-sm font-medium flex items-center gap-2">
					<FileText class="h-3.5 w-3.5 text-primary" />
					Description
					<span class="text-xs text-muted-foreground font-normal">(optional)</span>
				</Label>
				<Textarea
					id="edit-channel-description"
					bind:value={description}
					placeholder="Describe the purpose of this channel..."
					disabled={updating}
					rows={3}
					class="resize-none bg-background/80 border-border/50 focus-visible:ring-1 focus-visible:ring-primary focus-visible:border-primary"
				/>
			</div>
		</div>

		<Sheet.Footer class="flex-row gap-3 pb-6">
			<Button
				variant="outline"
				onclick={() => handleOpenChange(false)}
				disabled={updating}
				class="flex-1"
			>
				Cancel
			</Button>
			<Button onclick={handleSubmit} disabled={!canSubmit} class="flex-1">
				{#if updating}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Updating...
				{:else}
					Save Changes
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
