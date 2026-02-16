<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Label } from '$lib/components/ui/label';
	import { Textarea } from '$lib/components/ui/textarea';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Radio, LoaderCircle, Hash, FileText } from '@lucide/svelte';
	import { validateSlug } from '$lib/utils/channel';

	interface Props {
		open: boolean;
		creating: boolean;
		onOpenChange: (open: boolean) => void;
		onSubmit: (name: string, slug: string, description: string | undefined) => void;
	}

	let { open, creating, onOpenChange, onSubmit }: Props = $props();

	let name: string = $state('');
	let slug: string = $state('');
	let description: string = $state('');
	let slugError: string = $state('');

	function handleSlugInput(): void {
		if (slug) {
			const result = validateSlug(slug);
			slugError = result.error;
		} else {
			slugError = '';
		}
	}

	function handleSubmit(): void {
		const validation = validateSlug(slug);
		if (!name.trim() || !validation.valid) {
			slugError = validation.error;
			return;
		}
		onSubmit(name.trim(), slug.trim(), description.trim() || undefined);
		// Reset form
		name = '';
		slug = '';
		description = '';
		slugError = '';
	}

	function handleOpenChange(newOpen: boolean): void {
		onOpenChange(newOpen);
		if (!newOpen) {
			// Reset form when closing
			name = '';
			slug = '';
			description = '';
			slugError = '';
		}
	}

	const canSubmit: boolean = $derived(
		name.trim().length > 0 && slug.trim().length > 0 && !slugError && !creating
	);
</script>

<Sheet.Root {open} onOpenChange={handleOpenChange}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6 sm:max-w-md">
		<Sheet.Header class="pt-6">
			<Sheet.Title class="flex items-center gap-3 text-lg">
				<div class="flex h-9 w-9 items-center justify-center rounded-md bg-primary/10 border border-primary/20">
					<Radio class="h-4 w-4 text-primary" />
				</div>
				Create Channel
			</Sheet.Title>
			<Sheet.Description class="text-sm text-muted-foreground">
				Create a new channel for organizing your events and notifications.
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1 py-6 space-y-4">
			<!-- Channel Name & Slug Card -->
			<div class="rounded-md border border-border/50 bg-muted/20 p-4 space-y-4">
				<!-- Channel Name -->
				<div class="space-y-2">
					<Label for="channel-name" class="text-sm font-medium flex items-center gap-2">
						<Radio class="h-3.5 w-3.5 text-primary" />
						Channel Name <span class="text-destructive">*</span>
					</Label>
					<input
						id="channel-name"
						type="text"
						bind:value={name}
						placeholder="e.g., Production Events"
						disabled={creating}
						class="flex h-9 w-full rounded-md border border-border/50 bg-background/80 px-3 text-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
					/>
					<p class="text-xs text-muted-foreground">
						A descriptive name to identify this channel
					</p>
				</div>

				<!-- Slug -->
				<div class="space-y-2">
					<Label for="channel-slug" class="text-sm font-medium flex items-center gap-2">
						<Hash class="h-3.5 w-3.5 text-primary" />
						Slug <span class="text-destructive">*</span>
					</Label>
					<input
						id="channel-slug"
						type="text"
						bind:value={slug}
						oninput={handleSlugInput}
						placeholder="e.g., myapp.prod.errors"
						disabled={creating}
						class="flex h-9 w-full rounded-md border border-border/50 bg-background/80 px-3 text-sm font-mono transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
						class:border-destructive={slugError}
						class:focus-visible:ring-destructive={slugError}
					/>
					{#if slugError}
						<p class="text-xs text-destructive">{slugError}</p>
					{:else}
						<p class="text-xs text-muted-foreground">
							Unique identifier using lowercase letters, numbers, and dots
						</p>
					{/if}
				</div>
			</div>

			<!-- Description Card -->
			<div class="rounded-md border border-border/50 bg-muted/20 p-4 space-y-2">
				<Label for="channel-description" class="text-sm font-medium flex items-center gap-2">
					<FileText class="h-3.5 w-3.5 text-primary" />
					Description
					<span class="text-xs text-muted-foreground font-normal">(optional)</span>
				</Label>
				<Textarea
					id="channel-description"
					bind:value={description}
					placeholder="Describe the purpose of this channel..."
					disabled={creating}
					rows={3}
					class="resize-none bg-background/80 border-border/50 focus-visible:ring-1 focus-visible:ring-primary focus-visible:border-primary"
				/>
			</div>
		</div>

		<Sheet.Footer class="flex-row gap-3 pb-6">
			<Button
				variant="outline"
				onclick={() => handleOpenChange(false)}
				disabled={creating}
				class="flex-1"
			>
				Cancel
			</Button>
			<Button onclick={handleSubmit} disabled={!canSubmit} class="flex-1">
				{#if creating}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Creating...
				{:else}
					Create Channel
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
