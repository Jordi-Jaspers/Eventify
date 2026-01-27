<script lang="ts">
	import { Card, CardHeader, CardTitle, CardContent } from '$lib/components/ui/card';
	import { Hash } from '@lucide/svelte';

	interface Props {
		slug: string;
	}

	let { slug }: Props = $props();
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
	<CardHeader>
		<CardTitle class="text-xl flex items-center gap-2">
			<Hash class="w-5 h-5 text-primary" />
			Organization Identifier
		</CardTitle>
	</CardHeader>
	<CardContent>
		<div class="flex items-center gap-3 p-4 rounded-lg bg-background/50 border border-border/50">
			<div class="p-2 rounded-lg bg-primary/10">
				<Hash class="w-4 h-4 text-primary" />
			</div>
			<div class="flex-1">
				<p class="text-xs text-muted-foreground mb-1">Slug</p>
				<p class="text-lg font-mono text-foreground">{slug}</p>
			</div>
		</div>
	</CardContent>
</Card>
