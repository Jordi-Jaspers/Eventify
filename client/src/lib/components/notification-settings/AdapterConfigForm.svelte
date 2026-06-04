<script lang="ts">
	import { Button } from '$lib/components/ui/button';
	import { Switch } from '$lib/components/ui/switch';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { ExternalLink } from '@lucide/svelte';
	import { MattermostIcon, SlackIcon } from '$lib/components/icons';
	import { cn } from '$lib/utils';
	import type { AdapterType, CreateAdapterConfigRequest, UpdateAdapterConfigRequest } from '$lib/api/models';
	import AdapterTestButton from './AdapterTestButton.svelte';

	type WebhookAdapterType = 'MATTERMOST' | 'SLACK';

	interface InitialData {
		label: string;
		webhookUrl?: string;
		enabled: boolean;
		email?: string;
	}

	interface Props {
		mode: 'add' | 'edit';
		adapterType?: AdapterType;
		initialData?: InitialData;
		saving?: boolean;
		onSubmit: (req: CreateAdapterConfigRequest | UpdateAdapterConfigRequest) => Promise<void>;
		onCancel: () => void;
		onTest?: (adapterType: string, webhookUrl: string) => Promise<void>;
	}

	let { mode, adapterType, initialData, saving = false, onSubmit, onCancel, onTest }: Props = $props();

	// intentional: form state initialized from prop once (uncontrolled form pattern)
	/* eslint-disable svelte/reactive-component-state */
	let selectedType = $state<WebhookAdapterType>(
		mode === 'edit' ? (adapterType as WebhookAdapterType) : 'MATTERMOST'
	);
	let label = $state(initialData?.label ?? '');
	let webhookUrl = $state(initialData?.webhookUrl ?? '');
	let enabled = $state(initialData?.enabled ?? true);

	let labelError = $state('');
	let webhookError = $state('');

	const effectiveAdapterType = $derived<AdapterType>(
		mode === 'edit' ? (adapterType as AdapterType) : selectedType
	);
	const isWebhookAdapter = $derived(
		effectiveAdapterType === 'MATTERMOST' || effectiveAdapterType === 'SLACK'
	);

	const canSubmit = $derived(
		label.trim().length > 0 &&
		!labelError &&
		!webhookError &&
		(!isWebhookAdapter || webhookUrl.trim().length > 0)
	);

	function validateLabel(): void {
		labelError = label.trim() ? '' : 'Label is required';
	}

	function validateWebhook(): void {
		if (!isWebhookAdapter) return;
		if (!webhookUrl.trim()) {
			webhookError = 'Webhook URL is required';
		} else if (!webhookUrl.startsWith('http')) {
			webhookError = 'Must be a valid URL';
		} else {
			webhookError = '';
		}
	}

	async function handleSubmit(): Promise<void> {
		validateLabel();
		if (isWebhookAdapter) validateWebhook();
		if (!canSubmit) return;

		const config: Record<string, unknown> = isWebhookAdapter ? { webhookUrl } : {};

		if (mode === 'add') {
			await onSubmit({
				adapterType: selectedType,
				label: label.trim(),
				config,
				enabled
			} as CreateAdapterConfigRequest);
		} else {
			await onSubmit({
				label: label.trim(),
				config,
				enabled
			} as UpdateAdapterConfigRequest);
		}
	}

	const typeOptions: { value: WebhookAdapterType; label: string }[] = [
		{ value: 'MATTERMOST', label: 'Mattermost' },
		{ value: 'SLACK', label: 'Slack' }
	];
</script>

<form
	class="space-y-4"
	onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}
	aria-label="{mode === 'add' ? 'Add' : 'Edit'} {effectiveAdapterType} adapter"
>
	<!-- Type selector (add mode only) -->
	{#if mode === 'add'}
		<div class="space-y-1.5">
			<Label>Type</Label>
			<div class="flex gap-2">
				{#each typeOptions as opt}
					<button
						type="button"
						onclick={() => (selectedType = opt.value)}
						class={cn(
							'flex items-center gap-2 px-3 py-2 rounded-md border text-sm font-medium transition-colors',
							selectedType === opt.value
								? 'border-primary bg-accent/50 text-foreground'
								: 'border-border text-muted-foreground hover:border-primary/50 hover:text-foreground'
						)}
						aria-pressed={selectedType === opt.value}
					>
						{#if opt.value === 'MATTERMOST'}
							<MattermostIcon class="w-4 h-4" />
						{:else}
							<SlackIcon class="w-4 h-4" />
						{/if}
						{opt.label}
					</button>
				{/each}
			</div>
		</div>
	{:else}
		<!-- Read-only type badge (edit mode) -->
		<div class="flex items-center gap-2 text-sm text-muted-foreground">
			{#if effectiveAdapterType === 'MATTERMOST'}
				<MattermostIcon class="w-4 h-4" />
				<span class="font-medium text-foreground">Mattermost</span>
			{:else if effectiveAdapterType === 'SLACK'}
				<SlackIcon class="w-4 h-4" />
				<span class="font-medium text-foreground">Slack</span>
			{/if}
		</div>
	{/if}

	<!-- Label -->
	<div class="space-y-1">
		<Label for="adapter-label">Label</Label>
		<Input
			id="adapter-label"
			bind:value={label}
			placeholder="e.g. My Mattermost Notifier"
			oninput={validateLabel}
			aria-describedby={labelError ? 'label-error' : undefined}
			aria-invalid={!!labelError}
		/>
		{#if labelError}
			<p id="label-error" class="text-xs text-destructive">{labelError}</p>
		{/if}
	</div>

	<!-- Webhook URL (Mattermost / Slack) -->
	{#if isWebhookAdapter}
		<div class="space-y-1">
			<Label for="adapter-webhook">Webhook URL</Label>
			<div class="flex gap-2">
				<Input
					id="adapter-webhook"
					type="url"
					bind:value={webhookUrl}
					placeholder="https://hooks.example.com/…"
					oninput={validateWebhook}
					aria-describedby={webhookError ? 'webhook-error' : undefined}
					aria-invalid={!!webhookError}
					class="flex-1"
				/>
				{#if onTest}
					<AdapterTestButton
						adapterType={effectiveAdapterType}
						webhookUrl={webhookUrl}
						onTest={onTest}
					/>
				{/if}
			</div>
			{#if webhookError}
				<p id="webhook-error" class="text-xs text-destructive">{webhookError}</p>
			{/if}
			{#if effectiveAdapterType === 'MATTERMOST'}
				<a href="https://developers.mattermost.com/integrate/webhooks/incoming/" target="_blank" rel="noopener noreferrer" class="inline-flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground transition-colors">
					<ExternalLink class="w-3 h-3" />
					How to create a Mattermost webhook
				</a>
			{:else if effectiveAdapterType === 'SLACK'}
				<a href="https://api.slack.com/messaging/webhooks" target="_blank" rel="noopener noreferrer" class="inline-flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground transition-colors">
					<ExternalLink class="w-3 h-3" />
					How to create a Slack webhook
				</a>
			{/if}
		</div>
	{/if}

	<!-- Email read-only display -->
	{#if effectiveAdapterType === 'EMAIL'}
		<div class="space-y-1">
			<Label>Email Address</Label>
			<div class="px-3 py-2 rounded-md bg-muted/50 border border-border/50 text-sm text-muted-foreground">
				{initialData?.email ?? 'Your account email will be used'}
			</div>
			<p class="text-xs text-muted-foreground">Notifications are sent to your account email address.</p>
		</div>
	{/if}

	<!-- Enabled toggle -->
	<div class="flex items-center gap-3">
		<Switch id="adapter-enabled" bind:checked={enabled} />
		<Label for="adapter-enabled">Enable this adapter</Label>
	</div>

	<!-- Actions -->
	<div class="flex gap-2 pt-2">
		<Button type="submit" disabled={!canSubmit || saving} size="sm">
			{#if saving}Saving…{:else}{mode === 'add' ? 'Add Adapter' : 'Save Changes'}{/if}
		</Button>
		<Button type="button" variant="outline" size="sm" onclick={onCancel}>
			Cancel
		</Button>
	</div>
</form>
