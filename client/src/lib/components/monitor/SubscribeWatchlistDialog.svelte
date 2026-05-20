<script lang="ts">
	import { Bell, BellRing, Lock, LoaderCircle } from '@lucide/svelte';
	import * as Dialog from '$lib/components/ui/dialog';
	import { Button } from '$lib/components/ui/button';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { Label } from '$lib/components/ui/label';
	import { Separator } from '$lib/components/ui/separator';
	import { onMount } from 'svelte';
	import { toast } from 'svelte-sonner';
	import { handleError, formatValidationErrors } from '$lib/utils/error-handler';
	import {
		getSubscription,
		subscribe,
		unsubscribe
	} from '$lib/api/watchlist/WatchlistSubscriptionController';
	import type { SubscriptionResponse } from '$lib/api/models';

	interface Props {
		watchlistId: number;
	}

	let { watchlistId }: Props = $props();

	let open: boolean = $state(false);
	let loading: boolean = $state(false);
	let saving: boolean = $state(false);
	let subscription: SubscriptionResponse | null = $state(null);

	// Severity checkboxes
	let criticalChecked: boolean = $state(true);
	let warningChecked: boolean = $state(false);
	let okChecked: boolean = $state(false);

	const isSubscribed: boolean = $derived(subscription !== null);
	const hasSelection: boolean = $derived(criticalChecked || warningChecked || okChecked);

	function populateFromSubscription(sub: SubscriptionResponse | null): void {
		if (sub) {
			criticalChecked = sub.targetSeverities.includes('CRITICAL');
			warningChecked = sub.targetSeverities.includes('WARNING');
			okChecked = sub.targetSeverities.includes('OK');
		} else {
			criticalChecked = true;
			warningChecked = false;
			okChecked = false;
		}
	}

	async function loadSubscription(): Promise<void> {
		loading = true;
		try {
			subscription = await getSubscription(watchlistId);
			populateFromSubscription(subscription);
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load subscription');
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	async function handleSave(): Promise<void> {
		const severities: string[] = [];
		if (criticalChecked) severities.push('CRITICAL');
		if (warningChecked) severities.push('WARNING');
		if (okChecked) severities.push('OK');

		if (severities.length === 0) {
			toast.error('Select at least one severity level');
			return;
		}

		const wasSubscribed: boolean = isSubscribed;
		saving = true;
		try {
			subscription = await subscribe(watchlistId, {
				targetSeverities: severities,
				adapters: ['IN_APP']
			});
			populateFromSubscription(subscription);
			toast.success(wasSubscribed ? 'Notification settings saved' : 'Subscribed to watchlist');
			open = false;
		} catch (err: unknown) {
			const { message, validationErrors } = handleError(err, 'Failed to save subscription');
			toast.error(validationErrors ? formatValidationErrors(validationErrors) : message);
		} finally {
			saving = false;
		}
	}

	async function handleUnsubscribe(): Promise<void> {
		saving = true;
		try {
			await unsubscribe(watchlistId);
			subscription = null;
			populateFromSubscription(null);
			toast.success('Unsubscribed from watchlist');
			open = false;
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to unsubscribe');
			toast.error(message);
		} finally {
			saving = false;
		}
	}

	onMount(() => {
		loadSubscription();
	});
</script>

{#snippet severityCheckbox(id: string, label: string, checked: boolean, onChange: (v: boolean) => void)}
	<div class="flex items-center gap-3">
		<Checkbox {id} {checked} onCheckedChange={(v) => onChange(!!v)} />
		<Label for={id} class="text-sm font-medium cursor-pointer">{label}</Label>
	</div>
{/snippet}

<!-- Bell trigger button -->
<Button
	variant="outline"
	size="icon"
	title="Severity Notifications"
	class={isSubscribed ? 'text-primary border-primary/50 bg-primary/5' : ''}
	onclick={() => (open = true)}
>
	{#if loading}
		<LoaderCircle class="h-4 w-4 animate-spin" />
	{:else if isSubscribed}
		<BellRing class="h-4 w-4" />
	{:else}
		<Bell class="h-4 w-4" />
	{/if}
</Button>

<Dialog.Root bind:open>
	<Dialog.Content class="sm:max-w-[420px] border-border/50 bg-card/95 backdrop-blur-xl">
		<Dialog.Header>
			<Dialog.Title class="flex items-center gap-2 text-primary">
				<Bell class="h-4 w-4" />
				{isSubscribed ? 'Notification Settings' : 'Subscribe to Watchlist'}
			</Dialog.Title>
			<Dialog.Description>
				{#if isSubscribed}
					Manage your notification preferences for this watchlist.
				{:else}
					Get notified when channels in this watchlist change severity. Choose which severity levels
					trigger a notification.
				{/if}
			</Dialog.Description>
		</Dialog.Header>

		<div class="space-y-5 py-2">
			<!-- Severity Section -->
			<div class="space-y-3">
				<p class="text-sm font-medium">Notify on Severity</p>
				<div class="space-y-2.5">
					{@render severityCheckbox('severity-critical', 'CRITICAL', criticalChecked, (v) => (criticalChecked = v))}
					{@render severityCheckbox('severity-warning', 'WARNING', warningChecked, (v) => (warningChecked = v))}
					{@render severityCheckbox('severity-ok', 'OK', okChecked, (v) => (okChecked = v))}
				</div>
			</div>

			<Separator class="bg-border/30" />

			<!-- Notification Channels Section -->
			<div class="space-y-3">
				<p class="text-sm font-medium">Notification Channels</p>
				<div class="space-y-2.5">
					<!-- IN_APP — always on -->
					<div class="flex items-center gap-3">
						<Checkbox id="adapter-in-app" checked={true} disabled />
						<Label for="adapter-in-app" class="text-sm font-medium cursor-default flex items-center gap-1.5">
							In-App
							<span class="inline-flex items-center gap-1 text-xs text-muted-foreground font-normal">
								<Lock class="h-3 w-3" />
								Always on
							</span>
						</Label>
					</div>
					<!-- Future adapters (EMAIL, SLACK, WEBHOOK) can be added here -->
				</div>
			</div>
		</div>

		<Dialog.Footer class="flex-col gap-2 sm:flex-col">
			<Button onclick={handleSave} disabled={saving || !hasSelection} class="w-full">
				{#if saving}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
				{/if}
				{isSubscribed ? 'Save Changes' : 'Subscribe'}
			</Button>
			{#if isSubscribed}
				<Button
					variant="outline"
					onclick={handleUnsubscribe}
					disabled={saving}
					class="w-full text-destructive border-destructive/50 hover:bg-destructive/10 hover:text-destructive"
				>
					Unsubscribe
				</Button>
			{/if}
		</Dialog.Footer>
	</Dialog.Content>
</Dialog.Root>
