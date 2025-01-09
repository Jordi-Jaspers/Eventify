<script>
	import { LoaderCircle } from 'lucide-svelte';
</script>

<div class="fixed inset-0 !z-[1000] flex h-screen w-screen items-center justify-center overflow-hidden overscroll-auto bg-background">
	<div class="flex h-screen flex-col items-center justify-center space-y-4">
		<div class="mx-auto flex w-[350px] flex-col justify-center space-y-6">
			<div class="flex flex-row items-center justify-center">
				<h1>Eventify.io</h1>
			</div>
		</div>
		<LoaderCircle class="h-8 w-8 animate-spin" />
	</div>
</div>
