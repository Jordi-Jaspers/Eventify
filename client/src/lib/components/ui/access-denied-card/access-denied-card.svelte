<script lang="ts">
	import { Card, CardContent } from '$lib/components/ui/card';
	import { Shield } from '@lucide/svelte';
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
	<CardContent class="py-12 text-center">
		<div class="flex flex-col items-center">
			<div class="p-4 rounded-2xl bg-destructive/10 border border-destructive/30 mb-4">
				<Shield class="w-10 h-10 text-destructive" />
			</div>
			<h3 class="text-lg font-semibold mb-2">Access Denied</h3>
			<p class="text-muted-foreground">You do not have access to this organization.</p>
		</div>
	</CardContent>
</Card>
