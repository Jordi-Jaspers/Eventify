<script lang="ts">
	import OAuthButton from './OAuthButton.svelte';

	interface Props {
		disabled?: boolean;
		class?: string;
	}

	let { disabled = false, class: className = '' }: Props = $props();
</script>

<div class="space-y-4">
	<!-- OR Separator -->
	<div class="relative my-6">
		<div class="absolute inset-0 flex items-center">
			<div class="w-full border-t border-border/50"></div>
		</div>
		<div class="relative flex justify-center text-xs uppercase">
			<span class="bg-card px-2 text-muted-foreground">Or continue with</span>
		</div>
	</div>

	<!-- OAuth Provider Buttons -->
	<div class="grid grid-cols-2 gap-3 {className}">
		<OAuthButton provider="google" {disabled} />
		<OAuthButton provider="github" {disabled} />
	</div>
</div>
