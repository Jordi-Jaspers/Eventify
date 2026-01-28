<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Label } from '$lib/components/ui/label';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Key, LoaderCircle, Calendar, Tag, ShieldCheck, ShieldAlert } from '@lucide/svelte';

	interface Props {
		open: boolean;
		creating: boolean;
		onOpenChange: (open: boolean) => void;
		onSubmit: (name: string, expiresAt: string | undefined) => void;
	}

	let { open, creating, onOpenChange, onSubmit }: Props = $props();

	let name: string = $state('');
	let expirationDays: string = $state('90');

	const expirationOptions: Array<{ value: string; label: string; description: string }> = [
		{ value: '30', label: '30 days', description: 'Short-term access' },
		{ value: '90', label: '90 days', description: 'Recommended' },
		{ value: '365', label: '1 year', description: 'Long-term access' },
		{ value: 'never', label: 'Never', description: 'Less secure' }
	];

	function calculateExpiresAt(days: string): string | undefined {
		if (days === 'never') return undefined;
		const date: Date = new Date();
		date.setDate(date.getDate() + parseInt(days));
		return date.toISOString();
	}

	function handleSubmit(): void {
		if (!name.trim()) return;
		const expiresAt: string | undefined = calculateExpiresAt(expirationDays);
		onSubmit(name.trim(), expiresAt);
		// Reset form
		name = '';
		expirationDays = '90';
	}

	function handleOpenChange(newOpen: boolean): void {
		onOpenChange(newOpen);
		if (!newOpen) {
			// Reset form when closing
			name = '';
			expirationDays = '90';
		}
	}

	const canSubmit: boolean = $derived(name.trim().length > 0 && !creating);
	const isNeverExpire: boolean = $derived(expirationDays === 'never');
</script>

<Sheet.Root {open} onOpenChange={handleOpenChange}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6 sm:max-w-lg">
		<Sheet.Header class="pt-6">
			<Sheet.Title class="flex items-center gap-2 text-lg">
				<div
					class="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary/20 to-accent/20 border border-primary/30"
				>
					<Key class="h-4 w-4 text-primary" />
				</div>
				Create API Key
			</Sheet.Title>
			<Sheet.Description class="text-sm">
				Generate a new key for programmatic access. The full key will only be shown once.
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1 py-6 space-y-6">
			<!-- Name Input Card -->
			<div
				class="rounded-xl border border-border/50 bg-background/30 backdrop-blur-sm p-4 space-y-3"
			>
				<div class="flex items-center gap-2">
					<Tag class="h-4 w-4 text-primary" />
					<Label for="key-name" class="text-sm font-medium">Key Name</Label>
				</div>
				<input
					id="key-name"
					type="text"
					bind:value={name}
					placeholder="e.g., Production Server, CI/CD Pipeline"
					disabled={creating}
					class="flex h-10 w-full rounded-lg border border-border/50 bg-background/50 px-3 text-sm shadow-sm transition-all placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/30 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
				/>
				<p class="text-xs text-muted-foreground">
					Use a descriptive name to identify this key's purpose
				</p>
			</div>

			<!-- Expiration Card -->
			<div
				class="rounded-xl border border-border/50 bg-background/30 backdrop-blur-sm p-4 space-y-3"
			>
				<div class="flex items-center gap-2">
					<Calendar class="h-4 w-4 text-primary" />
					<Label class="text-sm font-medium">Expiration</Label>
				</div>
				<div class="grid grid-cols-2 gap-2">
					{#each expirationOptions as option (option.value)}
						<button
							type="button"
							onclick={() => (expirationDays = option.value)}
							disabled={creating}
							class="relative flex flex-col items-start p-3 rounded-lg border transition-all text-left {expirationDays ===
							option.value
								? 'border-primary bg-primary/10 ring-1 ring-primary/30'
								: 'border-border/50 bg-background/50 hover:border-border hover:bg-background/70'} disabled:cursor-not-allowed disabled:opacity-50"
						>
							<span class="text-sm font-medium">{option.label}</span>
							<span class="text-xs text-muted-foreground">{option.description}</span>
						</button>
					{/each}
				</div>

				<!-- Security hint -->
				{#if isNeverExpire}
					<div
						class="flex items-start gap-2 p-2.5 rounded-lg bg-yellow-500/10 border border-yellow-500/30 text-yellow-600 dark:text-yellow-500"
					>
						<ShieldAlert class="h-4 w-4 flex-shrink-0 mt-0.5" />
						<p class="text-xs">
							Non-expiring keys are convenient but pose a security risk if compromised.
						</p>
					</div>
				{:else}
					<div
						class="flex items-start gap-2 p-2.5 rounded-lg bg-green-500/10 border border-green-500/30 text-green-600 dark:text-green-500"
					>
						<ShieldCheck class="h-4 w-4 flex-shrink-0 mt-0.5" />
						<p class="text-xs">Expiring keys are more secure and follow best practices.</p>
					</div>
				{/if}
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
				class="flex-1"
			>
				{#if creating}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Creating...
				{:else}
					<Key class="mr-2 h-4 w-4" />
					Create Key
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
