<script lang="ts">
	import { Wrench } from '@lucide/svelte';
</script>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto animate-fade-in">
		<div class="flex flex-col items-center justify-center py-24 gap-4 text-center">
			<div class="rounded-full bg-muted/50 p-6">
				<Wrench class="h-10 w-10 text-muted-foreground" />
			</div>
			<h2 class="text-xl font-semibold text-foreground">No tools available yet</h2>
			<p class="text-muted-foreground max-w-sm">
				Admin tools will appear here as they are added.
			</p>
		</div>
	</div>
</main>
