<script lang="ts">
	import { user } from '$lib/store/global.js';
</script>

{#if user}
	Hello {user.fullName} watch monitoring!
{/if}
