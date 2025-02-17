<script lang="ts">
	import { WifiOff } from 'lucide-svelte';
</script>

<div class="flex h-64 flex-col items-center justify-center space-y-4">
	<WifiOff class="h-16 w-16 text-muted-foreground" />
	<div class="text-lg text-muted-foreground">Connection lost</div>
	<div class="text-sm text-muted-foreground/60">Unable to receive monitoring updates</div>
</div>
