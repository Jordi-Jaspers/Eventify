<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Card } from '$lib/components/ui/card';
	import { Copy, Check } from '@lucide/svelte';
	import { copyToClipboard } from '$lib/utils/clipboard';

	let copied: boolean = $state(false);

	async function copyCode(): Promise<void> {
		const code: string = `curl -X POST https://api.eventify.io/v1/external/event \\
  -H "X-API-Key: ev_live_abc123" \\
  -H "Content-Type: application/json" \\
  -d '{
    "slug": "deployment",
    "severity": "info",
    "title": "Deployment successful",
    "message": "v2.1.0 deployed to production",
    "metadata": {"service": "api-gateway", "region": "eu-west-1"}
  }'`;

		try {
			await copyToClipboard(code);
			copied = true;
			setTimeout(() => { copied = false; }, 2000);
		} catch {
			// copyToClipboard already shows error toast
		}
	}
</script>

<section class="py-20 px-4 sm:px-6 lg:px-8 bg-muted/30">
	<div class="container mx-auto max-w-4xl">
		<div class="text-center mb-12 animate-fade-in-up">
			<h2 class="text-3xl sm:text-4xl font-bold mb-4 text-foreground">
				Simple Integration
			</h2>
			<p class="text-lg text-muted-foreground">
				Start sending events with a single API call
			</p>
		</div>

		<div class="animate-fade-in-up">
			<Card class="bg-card/50 backdrop-blur-xl border border-border/50 shadow-md overflow-hidden">
				<div class="relative p-6 overflow-x-auto">
					<Button
						variant="ghost"
						size="sm"
						onclick={copyCode}
						class="absolute top-3 right-3 h-8 text-xs font-medium hover:bg-primary/20 hover:text-primary z-10"
						aria-label="Copy code"
					>
						{#if copied}
							<Check class="h-3 w-3 mr-1.5" />
							Copied!
						{:else}
							<Copy class="h-3 w-3 mr-1.5" />
							Copy
						{/if}
					</Button>
					<pre class="text-sm"><code class="language-bash text-muted-foreground"><span class="text-primary">curl</span> <span class="text-amber-500">-X POST</span> <span class="text-green-500">https://api.eventify.io/v1/external/event</span> <span class="text-amber-500">\</span>
  <span class="text-amber-500">-H</span> <span class="text-green-500">"X-API-Key: ev_live_abc123"</span> <span class="text-amber-500">\</span>
  <span class="text-amber-500">-H</span> <span class="text-green-500">"Content-Type: application/json"</span> <span class="text-amber-500">\</span>
  <span class="text-amber-500">-d</span> <span class="text-green-500">'{`{
    "slug": "deployment",
    "severity": "info",
    "title": "Deployment successful",
    "message": "v2.1.0 deployed to production",
    "metadata": {"service": "api-gateway", "region": "eu-west-1"}
  }`}'</span></code></pre>
				</div>
			</Card>

			<div class="text-center mt-8">
				<p class="text-sm text-muted-foreground">
					Want to learn more?
					<Button variant="link" class="text-primary hover:underline px-1 h-auto font-medium">
						See the documentation →
					</Button>
				</p>
			</div>
		</div>
	</div>
</section>
