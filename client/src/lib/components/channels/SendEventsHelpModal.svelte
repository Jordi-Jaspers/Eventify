<script lang="ts">
	import { CircleQuestionMark, Send, Zap, Package, Key } from '@lucide/svelte';
	import * as Dialog from '$lib/components/ui/dialog';
	import { Button } from '$lib/components/ui/button';
	import { Separator } from '$lib/components/ui/separator';
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
		{#snippet child({ props }: { props: Record<string, any> })}
			<Button
				{...props}
				variant="ghost"
				size="icon"
				class="h-8 w-8 text-muted-foreground hover:text-foreground hover:bg-muted/50 transition-all duration-200"
				aria-label="How to Send Events"
			>
				<CircleQuestionMark class="h-4 w-4" />
			</Button>
		{/snippet}
	</Dialog.Trigger>
	<Dialog.Content
		class="max-w-2xl p-0 border-border/50 bg-card/95 backdrop-blur-xl shadow-2xl max-h-[90vh] overflow-y-auto"
	>
		<!-- Header -->
		<Dialog.Header class="px-6 py-4 border-b border-border/30 sticky top-0 bg-card/95 backdrop-blur-xl z-10">
			<div class="flex items-center gap-2.5">
				<div
					class="p-2 rounded-lg bg-linear-to-br from-primary/20 to-accent/20 border border-primary/30"
				>
					<Send class="h-4 w-4 text-primary" />
				</div>
				<div>
					<Dialog.Title class="font-semibold text-sm">How to Send Events</Dialog.Title>
					<Dialog.Description class="text-xs text-muted-foreground">cURL examples for event ingestion</Dialog.Description>
				</div>
			</div>
		</Dialog.Header>

		<div class="p-6 space-y-5">
			<!-- API Key Note -->
			<div
				class="flex items-start gap-2 px-3 py-2.5 rounded-lg bg-primary/5 border border-primary/20"
			>
				<Key class="h-3.5 w-3.5 text-primary mt-0.5 shrink-0" />
				<p class="text-xs text-primary/90 leading-relaxed">
					Get your API key at <a
						href={apiKeySettingsUrl}
						class="underline font-medium hover:text-primary"
						onclick={() => (open = false)}>API Key Settings</a
					>
				</p>
			</div>

			<!-- Section 1: Single Event -->
			<div class="space-y-3">
				<div class="flex items-center gap-2">
					<Zap class="h-3.5 w-3.5 text-muted-foreground" />
					<div>
						<h4 class="text-xs font-semibold text-foreground/90">Single Event</h4>
						<p class="text-[10px] text-muted-foreground">Real-time event ingestion</p>
					</div>
				</div>

				<CodeBlockWithCopy
					code={singleEventCurl}
					headerText="POST /v1/external/event"
				/>
			</div>

			<Separator class="bg-border/30" />

			<!-- Section 2: Batch Insert -->
			<div class="space-y-3">
				<div class="flex items-center gap-2">
					<Package class="h-3.5 w-3.5 text-muted-foreground" />
					<div>
						<h4 class="text-xs font-semibold text-foreground/90">Batch Insert</h4>
						<p class="text-[10px] text-muted-foreground">
							Import historical data (all-or-nothing)
						</p>
					</div>
				</div>

				<CodeBlockWithCopy
					code={batchEventCurl}
					headerText="POST /v1/external/event/batch"
				/>

				<!-- Batch Note -->
				<div
					class="flex items-start gap-2 px-3 py-2 rounded-lg bg-amber-500/10 border border-amber-500/30"
				>
					<div class="mt-0.5">
						<div class="h-1 w-1 rounded-full bg-amber-500"></div>
					</div>
					<p class="text-xs text-amber-600 dark:text-amber-500 leading-relaxed">
						Use <code class="font-mono bg-amber-500/10 px-1 rounded">timestamp</code> field
						to import events with historical timestamps
					</p>
				</div>
			</div>
		</div>

		<!-- Footer hint -->
		<div class="px-6 py-3 border-t border-border/30 bg-muted/5 sticky bottom-0">
			<p class="text-[10px] text-muted-foreground text-center">
				Replace <code class="font-mono bg-muted px-1 rounded">&lt;YOUR_API_KEY&gt;</code> and
				<code class="font-mono bg-muted px-1 rounded">channelId</code> with your values
			</p>
		</div>
	</Dialog.Content>
</Dialog.Root>
