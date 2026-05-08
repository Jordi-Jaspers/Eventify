<script lang="ts">
	import { CircleHelp, Zap, Package, Key, ExternalLink, Terminal } from '@lucide/svelte';
	import * as Dialog from '$lib/components/ui/dialog';
	import { Button } from '$lib/components/ui/button';
	import { CodeBlockWithCopy } from '$lib/components/ui/code-block-with-copy';
	import { generateCurlCommand, generateBatchCurlCommand } from '$lib/utils/channel';

	interface Props {
		apiKeySettingsUrl: string;
	}

	let { apiKeySettingsUrl }: Props = $props();

	let open: boolean = $state(false);

	const singleEventCurl: string = $derived(generateCurlCommand(undefined));
	const batchEventCurl: string = $derived(generateBatchCurlCommand());
</script>

<Dialog.Root bind:open>
	<Dialog.Trigger>
		{#snippet child({ props }: { props: Record<string, unknown> })}
			<Button
				{...props}
				variant="ghost"
				size="icon"
				class="h-8 w-8 text-muted-foreground hover:text-foreground hover:bg-muted/50 transition-all duration-200"
				aria-label="How to Send Events"
			>
				<CircleHelp class="h-4 w-4" />
			</Button>
		{/snippet}
	</Dialog.Trigger>
	<Dialog.Content
		class="!max-w-[1100px] w-[95vw] p-0 border-border/50 bg-background shadow-2xl max-h-[90vh] overflow-hidden flex flex-col gap-0"
		showCloseButton={false}
	>
		<!-- Header -->
		<div class="flex items-center justify-between px-8 py-5 border-b border-border/50 bg-muted/30">
			<div class="flex items-center gap-4">
				<div class="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10">
					<Terminal class="h-6 w-6 text-primary" />
				</div>
				<div>
					<Dialog.Title class="text-xl font-semibold">Event Ingestion API</Dialog.Title>
					<Dialog.Description class="text-sm text-muted-foreground">
						Send events to your channels using cURL or any HTTP client
					</Dialog.Description>
				</div>
			</div>
			<a
				href={apiKeySettingsUrl}
				class="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-primary bg-primary/10 hover:bg-primary/15 rounded-lg transition-colors"
				onclick={() => (open = false)}
			>
				<Key class="h-4 w-4" />
				Get API Key
				<ExternalLink class="h-3.5 w-3.5" />
			</a>
		</div>

		<!-- Content - Two Column Layout -->
		<div class="flex-1 overflow-y-auto">
			<div class="grid grid-cols-1 lg:grid-cols-2 divide-y lg:divide-y-0 lg:divide-x divide-border/50">
				<!-- Single Event Column -->
				<div class="p-8 space-y-6">
					<div class="flex items-center gap-4">
						<div class="flex h-14 w-14 items-center justify-center rounded-2xl bg-amber-500/10">
							<Zap class="h-7 w-7 text-amber-500" />
						</div>
						<div>
							<h3 class="text-lg font-semibold">Single Event</h3>
							<p class="text-sm text-muted-foreground">Real-time ingestion</p>
						</div>
					</div>

					<p class="text-sm text-muted-foreground leading-relaxed">
						Send individual events as they occur. Ideal for real-time monitoring, live data streams, and immediate alerting.
					</p>

					<CodeBlockWithCopy code={singleEventCurl} headerText="POST /v1/external/event" />

					<div class="space-y-4">
						<p class="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Required Fields</p>
						<div class="grid gap-3">
							<div class="flex items-start gap-3">
								<code class="font-mono bg-muted px-2 py-1 rounded text-xs shrink-0 min-w-[90px]">channelId</code>
								<span class="text-sm text-muted-foreground">Target channel for the event</span>
							</div>
							<div class="flex items-start gap-3">
								<code class="font-mono bg-muted px-2 py-1 rounded text-xs shrink-0 min-w-[90px]">version</code>
								<span class="text-sm text-muted-foreground">Schema version identifier</span>
							</div>
							<div class="flex items-start gap-3">
								<code class="font-mono bg-muted px-2 py-1 rounded text-xs shrink-0 min-w-[90px]">severity</code>
								<span class="text-sm text-muted-foreground">INFO, WARNING, ERROR, or CRITICAL</span>
							</div>
							<div class="flex items-start gap-3">
								<code class="font-mono bg-muted px-2 py-1 rounded text-xs shrink-0 min-w-[90px]">summary</code>
								<span class="text-sm text-muted-foreground">Brief event description</span>
							</div>
						</div>
					</div>
				</div>

				<!-- Batch Insert Column -->
				<div class="p-8 space-y-6">
					<div class="flex items-center gap-4">
						<div class="flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-500/10">
							<Package class="h-7 w-7 text-blue-500" />
						</div>
						<div>
							<h3 class="text-lg font-semibold">Batch Insert</h3>
							<p class="text-sm text-muted-foreground">Bulk import</p>
						</div>
					</div>

					<p class="text-sm text-muted-foreground leading-relaxed">
						Import multiple events in a single request. All events are inserted atomically — if one fails, none are saved.
					</p>

					<CodeBlockWithCopy code={batchEventCurl} headerText="POST /v1/external/event/batch" />

					<div class="space-y-4">
						<p class="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Additional Options</p>
						<div class="grid gap-3">
							<div class="flex items-start gap-3">
								<code class="font-mono bg-muted px-2 py-1 rounded text-xs shrink-0 min-w-[90px]">timestamp</code>
								<span class="text-sm text-muted-foreground">Custom timestamp for historical data</span>
							</div>
							<div class="flex items-start gap-3">
								<code class="font-mono bg-muted px-2 py-1 rounded text-xs shrink-0 min-w-[90px]">metadata</code>
								<span class="text-sm text-muted-foreground">Additional JSON key-value pairs</span>
							</div>
							<div class="flex items-start gap-3">
								<code class="font-mono bg-muted px-2 py-1 rounded text-xs shrink-0 min-w-[90px]">tags</code>
								<span class="text-sm text-muted-foreground">Array of string tags for filtering</span>
							</div>
						</div>
					</div>

					<!-- Batch Tip -->
					<div class="flex items-start gap-3 p-4 rounded-xl bg-amber-500/5 border border-amber-500/20">
						<Zap class="h-5 w-5 text-amber-500 mt-0.5 shrink-0" />
						<div>
							<p class="text-sm font-medium text-amber-600 dark:text-amber-400">Pro Tip</p>
							<p class="text-sm text-muted-foreground mt-1">
								Use the timestamp field to backfill historical events with their original occurrence time.
							</p>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- Footer -->
		<div class="flex items-center justify-between px-8 py-4 border-t border-border/50 bg-muted/20">
			<p class="text-sm text-muted-foreground">
				Replace <code class="font-mono bg-muted px-1.5 py-0.5 rounded text-xs">&lt;YOUR_API_KEY&gt;</code> with your API key from settings
			</p>
			<Button variant="outline" onclick={() => (open = false)}>
				Close
			</Button>
		</div>
	</Dialog.Content>
</Dialog.Root>
