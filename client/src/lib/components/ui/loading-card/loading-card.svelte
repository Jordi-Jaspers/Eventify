<script lang="ts">
	import { Card, CardContent } from '$lib/components/ui/card';
	import { Loader2 } from '@lucide/svelte';
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
	<CardContent class="py-12 flex flex-col items-center justify-center">
		<Loader2 class="w-8 h-8 animate-spin text-primary mb-3" />
		<span class="text-muted-foreground">Loading organization...</span>
	</CardContent>
</Card>
