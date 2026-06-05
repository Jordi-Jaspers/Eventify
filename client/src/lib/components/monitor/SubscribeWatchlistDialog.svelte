<script lang="ts">
	import { Bell, BellRing, LoaderCircle, ExternalLink } from '@lucide/svelte';
	import * as Dialog from '$lib/components/ui/dialog';
	import { Button } from '$lib/components/ui/button';
	import { Separator } from '$lib/components/ui/separator';
	import { onMount } from 'svelte';
	import { toast } from 'svelte-sonner';
	import { handleError, formatValidationErrors } from '$lib/utils/error-handler';
	import { listPersonalConfigs } from '$lib/api/notification/AdapterConfigController';
	import { createPersonalSubscription, updatePersonalSubscription, deletePersonalSubscription, getSubscriptionByWatchlistId } from '$lib/api/subscription/SubscriptionController';
	import type { SubscriptionResponse, AdapterConfigResponse } from '$lib/api/models';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import AdapterChecklist from '$lib/components/subscriptions/AdapterChecklist.svelte';
	import SeverityPicker from '$lib/components/subscriptions/SeverityPicker.svelte';

	interface Props {
		watchlistId: number;
		/** Set true if watchlist belongs to an org */
		isOrgWatchlist?: boolean;
	}

	let { watchlistId, isOrgWatchlist = false }: Props = $props();

	let open: boolean = $state(false);
	let loading: boolean = $state(false);
	let saving: boolean = $state(false);
	let subscription: SubscriptionResponse | null = $state(null);

	// Severity state
	let selectedSeverities: string[] = $state(['CRITICAL']);

	// Adapters
	let adapterConfigs: AdapterConfigResponse[] = $state([]);
	let selectedAdapterIds: Set<string> = $state(new Set());
	let loadingAdapters: boolean = $state(false);

	const isSubscribed: boolean = $derived(subscription !== null);
	const hasSelection: boolean = $derived(selectedSeverities.length > 0);

	function populateFromSubscription(sub: SubscriptionResponse | null): void {
		if (sub) {
			selectedSeverities = [...sub.targetSeverities];
			selectedAdapterIds = new Set(sub.adapterConfigIds);
		} else {
			selectedSeverities = ['CRITICAL'];
			selectedAdapterIds = new Set();
		}
	}

	async function loadSubscription(): Promise<void> {
		loading = true;
		try {
			subscription = await getSubscriptionByWatchlistId(watchlistId);
			populateFromSubscription(subscription);
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load subscription');
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	async function loadAdapters(): Promise<void> {
		loadingAdapters = true;
		try {
			adapterConfigs = await listPersonalConfigs();
		} catch {
			// Non-critical — silently skip
		} finally {
			loadingAdapters = false;
		}
	}

	function toggleAdapter(id: string, checked: boolean): void {
		const next = new Set(selectedAdapterIds);
		if (checked) next.add(id);
		else next.delete(id);
		selectedAdapterIds = next;
	}

	async function handleSave(): Promise<void> {
		const severities = selectedSeverities as ('CRITICAL' | 'WARNING' | 'OK' | 'NO_DATA')[];
		if (severities.length === 0) {
			toast.error('Select at least one severity level');
			return;
		}

		const inAppIds = adapterConfigs
			.filter((c) => c.adapterType === 'IN_APP')
			.map((c) => String(c.id));
		const adapterConfigIds: string[] = Array.from(new Set([...selectedAdapterIds, ...inAppIds]));
		const wasSubscribed: boolean = isSubscribed;
		saving = true;
		try {
			if (subscription) {
				subscription = await updatePersonalSubscription(subscription.id, { targetSeverities: severities, adapterConfigIds });
			} else {
				subscription = await createPersonalSubscription({ watchlistId, targetSeverities: severities, adapterConfigIds });
			}
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
		if (!subscription) return;
		saving = true;
		try {
			await deletePersonalSubscription(subscription.id);
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
		loadAdapters();
	});
</script>

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
	<Dialog.Content class="sm:max-w-[440px] border-border/50 bg-card/95 backdrop-blur-xl">
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
				<SeverityPicker
					selected={selectedSeverities}
					onChange={(s) => (selectedSeverities = s)}
				/>
			</div>

			<Separator class="bg-border/30" />

			<!-- Notification Adapters Section -->
			<div class="space-y-3">
				<p class="text-sm font-medium">Delivery Adapters</p>
			{#if loadingAdapters}
				<div class="flex items-center gap-2 text-sm text-muted-foreground">
					<LoaderCircle class="h-4 w-4 animate-spin" />
					Loading adapters…
				</div>
			{:else}
				<AdapterChecklist
					configs={adapterConfigs}
					selectedIds={selectedAdapterIds}
					onToggle={toggleAdapter}
				>
					{#snippet emptyState()}
						<div class="px-3 py-2.5">
							<p class="text-xs text-muted-foreground">
								No external adapters configured.
								<a
									href={CLIENT_ROUTES.PROFILE_NOTIFICATIONS_PAGE.path}
									class="underline text-primary inline-flex items-center gap-1"
								>
									Link more in Settings <ExternalLink class="h-3 w-3" />
								</a>
							</p>
						</div>
					{/snippet}
				</AdapterChecklist>
			{/if}
			</div>

			{#if isOrgWatchlist}
				<p class="text-xs text-muted-foreground border border-border/40 rounded-md px-3 py-2 bg-muted/20">
					For org-wide notifications, configure in Org Settings → Subscriptions.
				</p>
			{/if}
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
