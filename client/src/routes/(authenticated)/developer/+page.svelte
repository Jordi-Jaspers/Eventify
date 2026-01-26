<script lang="ts">
	import { onMount } from 'svelte';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import Button from '$lib/components/ui/button/button.svelte';
	import * as Tooltip from '$lib/components/ui/tooltip';
	import { Key, Plus, Info } from '@lucide/svelte';
	import { SettingsNav } from '$lib/components/settings';
	import {
		ApiKeyList,
		CreateApiKeySheet,
		ApiKeyCreatedModal,
		QuotaProgressBar
	} from '$lib/components/api-keys';
	import { createApiKeyService } from '$lib/api/apikey/service/ApiKeyService.svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { getQuota } from '$lib/api/user/UserController';

	const apiKeyService = createApiKeyService();

	let showCreateSheet: boolean = $state(false);
	let creating: boolean = $state(false);
	let revoking: boolean = $state(false);

	let quotaUsed: number = $state(0);
	let quotaLimit: number = $state(1000);
	let quotaLoading: boolean = $state(true);

	onMount(() => {
		apiKeyService.loadKeys();
		loadQuota();
	});

	async function loadQuota(): Promise<void> {
		try {
			quotaLoading = true;
			const quota = await getQuota();
			quotaUsed = quota.used ?? 0;
			quotaLimit = quota.limit ?? 1000;
		} catch (error) {
			console.error('Failed to load quota:', error);
			// Fallback to default values on error
			quotaUsed = 0;
			quotaLimit = 1000;
		} finally {
			quotaLoading = false;
		}
	}

	async function handleCreateKey(name: string, expiresAt: string | undefined): Promise<void> {
		creating = true;
		const success: boolean = await apiKeyService.createKey({ name, expiresAt });
		creating = false;
		if (success) {
			showCreateSheet = false;
		}
	}

	async function handleRevokeKey(keyId: number): Promise<void> {
		revoking = true;
		await apiKeyService.revokeKey(keyId);
		revoking = false;
	}

	function handleCloseCreatedModal(): void {
		apiKeyService.clearCreatedKey();
	}

	const showCreatedModal: boolean = $derived(apiKeyService.createdKey !== null);
</script>

<svelte:head>
	<title>Developer Settings - Eventify</title>
</svelte:head>

<!-- Settings Navigation -->
<SettingsNav currentPath={CLIENT_ROUTES.DEVELOPER_PAGE.path} />

<!-- Main Content -->
<main class="container mx-auto px-4 py-8">
	<div class="max-w-5xl mx-auto space-y-6 animate-fade-in">
		<!-- Header Card -->
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg relative overflow-hidden">
			<!-- Gradient overlay -->
			<div
				class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"
			></div>

			<CardHeader class="relative z-10">
				<div class="flex items-start justify-between gap-4">
					<div class="space-y-1.5">
						<CardTitle class="text-2xl flex items-center gap-3">
							<Key class="w-6 h-6 text-primary" />
							API Keys
						</CardTitle>
						<CardDescription class="text-sm"> Manage API keys for programmatic access to Eventify </CardDescription>
					</div>
					{#if apiKeyService.atLimit}
						<Tooltip.Provider>
							<Tooltip.Root>
								<Tooltip.Trigger>
									{#snippet child({ props })}
										<Button
											{...props}
											disabled={true}
											class="opacity-50 cursor-not-allowed"
										>
								<Plus class="mr-2 h-4 w-4" />
								New Key
										</Button>
									{/snippet}
								</Tooltip.Trigger>
								<Tooltip.Content>
									<p>Maximum limit of {apiKeyService.limit} API keys reached</p>
								</Tooltip.Content>
							</Tooltip.Root>
						</Tooltip.Provider>
					{:else}
						<Button
							onclick={() => (showCreateSheet = true)}
							class=""
						>
							<Plus class="mr-2 h-4 w-4" />
							New Key
						</Button>
					{/if}
				</div>
			</CardHeader>

			<CardContent class="space-y-6 relative z-10">
				<!-- Monthly Quota -->
				<QuotaProgressBar used={quotaUsed} limit={quotaLimit} />

				<!-- API Keys List or Empty State -->
				{#if apiKeyService.loading}
					<div class="flex items-center justify-center py-12">
						<div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
					</div>
				{:else if apiKeyService.keys.length === 0}
					<!-- Empty State -->
					<div
						class="flex flex-col items-center justify-center py-16 px-6 text-center border-2 border-dashed border-border/50 rounded-lg bg-background/30"
					>
						<Key class="w-16 h-16 text-muted-foreground/30 mb-4" />
						<h3 class="text-lg font-semibold mb-2">No API Keys Yet</h3>
						<p class="text-sm text-muted-foreground max-w-md mb-6">
							API keys allow you to authenticate requests to the Eventify API. Create your first key
							to start sending events programmatically.
						</p>
						<Button
							onclick={() => (showCreateSheet = true)}
							class=""
						>
							<Plus class="mr-2 h-4 w-4" />
							Create Key
						</Button>
					</div>
				{:else}
					<ApiKeyList keys={apiKeyService.keys} onRevoke={handleRevokeKey} {revoking} />

					<!-- Key Limit Indicator (informational only) -->
					<div class="flex items-center gap-2 px-1">
						<Info class="w-3.5 h-3.5 text-muted-foreground/60" />
						<span class="text-xs text-muted-foreground/80">
							{apiKeyService.keys.length} of {apiKeyService.limit} keys used
						</span>
					</div>
				{/if}
			</CardContent>
		</Card>
	</div>
</main>

<!-- Modals/Sheets -->
<CreateApiKeySheet open={showCreateSheet} {creating} onOpenChange={(o) => (showCreateSheet = o)} onSubmit={handleCreateKey} />
<ApiKeyCreatedModal open={showCreatedModal} apiKey={apiKeyService.createdKey} onClose={handleCloseCreatedModal} />
