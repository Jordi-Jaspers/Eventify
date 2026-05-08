<script lang="ts">
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
	import { Slider } from '$lib/components/ui/slider';
	import { Database, AlertTriangle, LoaderCircle } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';

	interface RetentionOption {
		days: number;
		label: string;
		short: string;
	}

	interface Props {
		initialRetentionDays: number;
		loading: boolean;
		saving: boolean;
		onSave: (days: number) => Promise<void>;
	}

	let { initialRetentionDays, loading, saving, onSave }: Props = $props();

	const RETENTION_OPTIONS: RetentionOption[] = [
		{ days: 90, label: '3 months', short: '3mo' },
		{ days: 180, label: '6 months', short: '6mo' },
		{ days: 365, label: '1 year', short: '1y' },
		{ days: 730, label: '2 years', short: '2y' },
		{ days: 1095, label: '3 years', short: '3y' },
		{ days: 1825, label: '5 years', short: '5y' }
	];

	// Find initial index and create derived state
	let currentIndex: number = $state(2); // Default to 1 year

	// Reset currentIndex when initialRetentionDays changes
	$effect(() => {
		const idx: number = RETENTION_OPTIONS.findIndex(
			(opt: RetentionOption) => opt.days === initialRetentionDays
		);
		if (idx >= 0) {
			currentIndex = idx;
		}
	});

	// Current retention option
	const currentOption: RetentionOption = $derived(RETENTION_OPTIONS[currentIndex]);

	// Check if value changed
	const hasChanged: boolean = $derived(currentOption.days !== initialRetentionDays);

	// Check if reducing retention
	const isReducing: boolean = $derived(currentOption.days < initialRetentionDays);

	// Confirmation dialog state
	let showConfirmDialog: boolean = $state(false);

	function handleSliderChange(value: number): void {
		currentIndex = value;
	}

	function handleSaveClick(): void {
		if (isReducing) {
			showConfirmDialog = true;
		} else {
			handleConfirmedSave();
		}
	}

	async function handleConfirmedSave(): Promise<void> {
		showConfirmDialog = false;
		await onSave(currentOption.days);
	}
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
	<CardHeader>
		<CardTitle class="text-2xl flex items-center gap-2">
			<Database class="w-6 h-6 text-primary" />
			Data Retention
		</CardTitle>
		<CardDescription>
			Configure how long events are stored before automatic deletion
		</CardDescription>
	</CardHeader>

	<CardContent class="space-y-6">
		{#if loading}
			<div class="flex items-center justify-center py-8">
				<LoaderCircle class="h-8 w-8 animate-spin text-primary" />
			</div>
		{:else}
			<!-- Warning alert when reducing -->
			{#if isReducing}
				<Alert
					variant="default"
					class="bg-amber-500/10 border-amber-500/50 backdrop-blur-sm [&>svg]:text-amber-600 dark:[&>svg]:text-amber-400"
				>
					<AlertTriangle class="!h-5 !w-5" />
					<AlertDescription class="text-amber-700 dark:text-amber-300">
						Reducing retention will permanently delete events older than {currentOption.label}. This
						action cannot be undone.
					</AlertDescription>
				</Alert>
			{/if}

			<!-- Current value display -->
			<div class="text-center py-4">
				<p class="text-sm text-muted-foreground mb-2">Current Retention Period</p>
				<p class="text-4xl font-bold text-primary">{currentOption.label}</p>
			</div>

			<!-- Slider -->
			<div class="space-y-4">
				<Slider
					type="single"
					bind:value={currentIndex}
					onValueChange={handleSliderChange}
					min={0}
					max={RETENTION_OPTIONS.length - 1}
					step={1}
					class="w-full"
					disabled={saving}
				/>

				<!-- Labels -->
				<div class="flex justify-between px-2">
					{#each RETENTION_OPTIONS as option}
						<span
							class="text-xs transition-colors {currentOption.days === option.days
								? 'text-primary font-semibold'
								: 'text-muted-foreground'}"
						>
							{option.short}
						</span>
					{/each}
				</div>
			</div>

			<!-- Save button -->
			<Button
				onclick={handleSaveClick}
				disabled={!hasChanged || saving}
				class="w-full"
			>
				{#if saving}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Saving...
				{:else}
					Save Changes
				{/if}
			</Button>
		{/if}
	</CardContent>
</Card>

<!-- Confirmation Dialog -->
<AlertDialog.Root open={showConfirmDialog} onOpenChange={(o) => (showConfirmDialog = o)}>
	<AlertDialog.Content class="bg-card/95 backdrop-blur-xl border-border/50">
		<AlertDialog.Header>
			<AlertDialog.Title>Reduce Data Retention?</AlertDialog.Title>
			<AlertDialog.Description>
				Events older than {currentOption.label} will be permanently deleted. This cannot be undone.
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel disabled={saving}>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={handleConfirmedSave}
				disabled={saving}
				class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
			>
				{#if saving}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Saving...
				{:else}
					Confirm & Save
				{/if}
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
