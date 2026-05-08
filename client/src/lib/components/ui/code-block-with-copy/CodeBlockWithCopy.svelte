<script lang="ts">
	import { Copy, Check } from '@lucide/svelte';
	import { Button } from '$lib/components/ui/button';
	import { copyToClipboard } from '$lib/utils/clipboard';

	interface Props {
		/** The code content to display */
		code: string;
		/** The API endpoint or title shown in the header */
		headerText: string;
		/** Error message for copy action (default: 'Failed to copy to clipboard') */
		errorMessage?: string;
		/** Additional class for the container */
		class?: string;
	}

	let {
		code,
		headerText,
		errorMessage = 'Failed to copy to clipboard',
		class: className = ''
	}: Props = $props();

	let copied: boolean = $state(false);

	async function handleCopy(): Promise<void> {
		try {
			await copyToClipboard(code, errorMessage);
			copied = true;
			setTimeout(() => {
				copied = false;
			}, 2000);
		} catch {
			// Error already handled by copyToClipboard
		}
	}
</script>

<div class="rounded-lg border border-border/50 bg-background/30 backdrop-blur-sm {className}">
	<div class="flex items-center justify-between p-3 border-b border-border/30">
		<code class="text-xs text-muted-foreground font-mono">{headerText}</code>
		<Button onclick={handleCopy} size="sm" variant="ghost" class="h-7 w-7 p-0">
			<span class="relative flex items-center justify-center w-4 h-4">
				<Copy
					class="w-3.5 h-3.5 absolute transition-all duration-200 {copied
						? 'scale-0 opacity-0'
						: 'scale-100 opacity-100'}"
				/>
				<Check
					class="w-3.5 h-3.5 absolute text-green-500 transition-all duration-200 {copied
						? 'scale-100 opacity-100'
						: 'scale-0 opacity-0'}"
				/>
			</span>
		</Button>
	</div>
	<div class="p-3">
		<pre
			class="text-[10px] font-mono overflow-x-auto text-slate-300 bg-slate-900 rounded p-2 border border-slate-700/50">{code}</pre>
	</div>
</div>
