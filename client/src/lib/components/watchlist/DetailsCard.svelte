<script lang="ts">
	import { Card, CardHeader, CardTitle, CardContent } from '$lib/components/ui/card';
	import { Input } from '$lib/components/ui/input';
	import { Textarea } from '$lib/components/ui/textarea';
	import { Label } from '$lib/components/ui/label';
	import { Button } from '$lib/components/ui/button';
	import { FileText, Loader2, Check, ChevronDown } from '@lucide/svelte';
	import { slide } from 'svelte/transition';

	interface Props {
		name: string;
		description?: string;
		onNameChange: (name: string) => void;
		onDescriptionChange: (description: string) => void;
		isSaving: boolean;
		showSavedIndicator: boolean;
	}

	let {
		name = $bindable(),
		description = $bindable(),
		onNameChange,
		onDescriptionChange,
		isSaving,
		showSavedIndicator
	}: Props = $props();

	let descriptionOpen: boolean = $state(false);

	// Auto-expand if description has content
	$effect(() => {
		if (description && description.trim().length > 0) {
			descriptionOpen = true;
		}
	});

	function toggleDescription(): void {
		descriptionOpen = !descriptionOpen;
	}
</script>

<Card class="bg-card/50 backdrop-blur-xl border-border/50 shadow-lg">
	<CardHeader>
		<div class="flex items-center justify-between">
			<CardTitle class="text-lg flex items-center gap-2">
				<FileText class="h-5 w-5 text-primary" />
				Watchlist Details
			</CardTitle>

			<!-- Save Indicator -->
			{#if isSaving}
				<div class="flex items-center gap-2 text-sm text-muted-foreground">
					<Loader2 class="h-4 w-4 animate-spin" />
					<span>Saving...</span>
				</div>
			{:else if showSavedIndicator}
				<div class="flex items-center gap-2 text-sm text-green-600 animate-fade-in">
					<Check class="h-4 w-4" />
					<span>Saved</span>
				</div>
			{/if}
		</div>
	</CardHeader>
	<CardContent class="space-y-4">
		<div class="space-y-2">
			<Label for="name">
				Name <span class="text-destructive" aria-label="required">*</span>
			</Label>
			<Input
				id="name"
				bind:value={name}
				onchange={() => onNameChange(name)}
				placeholder="My Watchlist"
				class="bg-background/50 border-border"
				required
			/>
		</div>

		<div class="space-y-2">
			<div class="flex items-center justify-between">
				<Label for="description" class="text-sm">Description</Label>
				<Button 
					variant="ghost" 
					size="sm" 
					class="h-7 px-2 gap-1"
					onclick={toggleDescription}
				>
					<span class="text-xs text-muted-foreground">
						{descriptionOpen ? 'Hide' : 'Add'}
					</span>
					<ChevronDown class="h-3 w-3 text-muted-foreground transition-transform {descriptionOpen ? 'rotate-180' : ''}" />
				</Button>
			</div>
			{#if descriptionOpen}
				<div transition:slide={{ duration: 200 }}>
					<Textarea
						id="description"
						bind:value={description}
						onchange={() => onDescriptionChange(description ?? '')}
						placeholder="Optional description for this watchlist"
						class="bg-background/50 border-border min-h-[80px]"
					/>
				</div>
			{/if}
		</div>
	</CardContent>
</Card>
