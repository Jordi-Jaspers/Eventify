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
	<div class="flex items-center gap-4 my-6">
		<div class="flex-1 border-t border-border/50"></div>
		<span class="text-xs uppercase text-muted-foreground">Or continue with</span>
		<div class="flex-1 border-t border-border/50"></div>
	</div>

	<!-- OAuth Provider Buttons -->
	<div class="grid grid-cols-2 gap-3 {className}">
		<OAuthButton provider="google" {disabled} />
		<OAuthButton provider="github" {disabled} />
	</div>
</div>
