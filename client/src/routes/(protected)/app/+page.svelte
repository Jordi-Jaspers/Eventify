<script lang="ts">
	import { user } from '$lib/store/global.js';
</script>

{#if user}
	Welcome {user.fullName} to home page.
{/if}
