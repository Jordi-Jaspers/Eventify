<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Copy, CircleCheck, Terminal, Key, TriangleAlert, Shield } from '@lucide/svelte';
	import type { ApiKeyCreationResponse } from '$lib/api/models';
	import { toast } from 'svelte-sonner';

	interface Props {
		open: boolean;
		apiKey: ApiKeyCreationResponse | null;
		onClose: () => void;
	}

	let { open, apiKey, onClose }: Props = $props();

	let copied: boolean = $state(false);
	let curlCopied: boolean = $state(false);

	async function copyToClipboard(): Promise<void> {
		if (!apiKey?.key) return;
		try {
			await navigator.clipboard.writeText(apiKey.key);
			copied = true;
			toast.success('API key copied to clipboard');
			setTimeout(() => {
				copied = false;
			}, 2000);
		} catch (err: unknown) {
			toast.error('Failed to copy to clipboard');
		}
	}

	async function copyCurlToClipboard(): Promise<void> {
		if (!curlExample) return;
		try {
			await navigator.clipboard.writeText(curlExample);
			curlCopied = true;
			toast.success('cURL command copied to clipboard');
			setTimeout(() => {
				curlCopied = false;
			}, 2000);
		} catch (err: unknown) {
			toast.error('Failed to copy to clipboard');
		}
	}

	const curlExample: string = $derived(
		apiKey?.key
			? `curl -X POST https://api.eventify.dev/v1/events \\
  -H "Authorization: Bearer ${apiKey.key}" \\
  -H "Content-Type: application/json" \\
  -d '{"event":"user.signup","userId":"123"}'`
			: ''
	);
</script>

<Sheet.Root {open} onOpenChange={onClose}>
	<Sheet.Content
		class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6 sm:max-w-lg"
	>
		<Sheet.Header class="pt-6">
			<Sheet.Title class="flex items-center gap-2 text-lg">
				<div
					class="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-green-500/20 to-emerald-500/20 border border-green-500/30"
				>
					<CircleCheck class="h-4 w-4 text-green-500" />
				</div>
				API Key Created
			</Sheet.Title>
			<Sheet.Description class="text-sm">
				Copy your key now — it won't be shown again.
			</Sheet.Description>
		</Sheet.Header>

		{#if apiKey}
			<div class="flex-1 py-5 space-y-4">
				<!-- Key Display Card with integrated warning -->
				<div class="rounded-xl border border-border/50 bg-background/30 backdrop-blur-sm">
					<!-- Header -->
					<div class="flex items-center justify-between p-4 pb-3">
						<div class="flex items-center gap-2">
							<Key class="h-4 w-4 text-primary" />
							<span class="text-sm font-medium">{apiKey.name}</span>
						</div>
						<div
							class="flex items-center gap-1.5 text-xs text-yellow-600 dark:text-yellow-500"
						>
							<TriangleAlert class="w-3.5 h-3.5" />
							<span>Save now</span>
						</div>
					</div>

					<!-- Key value -->
					<div class="px-4 pb-4 space-y-2">
						<div class="relative">
							<code
								class="block p-3 pr-20 rounded-lg bg-background/70 border border-border/50 text-sm font-mono break-all select-all"
							>
								{apiKey.key}
							</code>
							<Button
								onclick={copyToClipboard}
								size="sm"
								variant={copied ? 'default' : 'outline'}
								class="absolute top-1/2 -translate-y-1/2 right-2 h-8 gap-1.5 {copied
									? 'bg-green-500 hover:bg-green-600 text-white'
									: 'bg-background/80 hover:bg-background'}"
							>
								{#if copied}
									<CircleCheck class="w-3.5 h-3.5" />
									Copied
								{:else}
									<Copy class="w-3.5 h-3.5" />
									Copy
								{/if}
							</Button>
						</div>
						<p class="text-xs text-muted-foreground">
							<Shield class="inline-block w-3.5 h-3.5 mr-1 text-muted-foreground" />
							Never share your API key or commit it to version control.
						</p>
					</div>
				</div>

				<!-- Example Usage -->
				<div class="rounded-xl border border-border/50 bg-background/30 backdrop-blur-sm">
					<div class="flex items-center justify-between p-4 pb-3">
						<div class="flex items-center gap-2">
							<Terminal class="h-4 w-4 text-muted-foreground" />
							<span class="text-sm font-medium">Example</span>
						</div>
						<Button
							onclick={copyCurlToClipboard}
							size="sm"
							variant="ghost"
							class="h-7 px-2 text-xs text-muted-foreground hover:text-foreground -mr-2"
						>
							{#if curlCopied}
								<CircleCheck class="w-3.5 h-3.5 mr-1 text-green-500" />
								Copied
							{:else}
								<Copy class="w-3.5 h-3.5 mr-1" />
								Copy
							{/if}
						</Button>
					</div>
					<div class="px-4 pb-4">
						<pre
							class="p-3 rounded-lg bg-slate-900 border border-slate-700/50 text-xs font-mono overflow-x-auto text-slate-300">{curlExample}</pre>
					</div>
				</div>
			</div>
		{/if}

		<Sheet.Footer class="flex-row gap-3 sm:flex-row pb-6">
			<Button
				onclick={onClose}
				class="flex-1 bg-gradient-to-r from-primary to-accent hover:opacity-90 shadow-lg shadow-primary/20"
			>
				Done
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
