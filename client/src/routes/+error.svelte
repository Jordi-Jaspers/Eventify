<script lang="ts">
import { page } from '$app/stores';
import Logo from "$lib/assets/images/404.webp?enhanced";
</script>

{#if $page.status === 404}
	<div class="flex h-screen flex-col items-center justify-center space-y-4">
		<div class="mx-auto flex w-max-[350px] flex-col justify-center space-y-6">
				<enhanced:img src={Logo} alt="Eventify Logo" />
		</div>
		<h1 class="mb-4 text-4xl font-bold">404 - Page Not Found</h1>
		<p class="mb-8 text-lg">Oops! The page you're looking for doesn't exist.</p>
		<a href="/dashboard" class="text-blue-500 hover:underline">Go back to the homepage</a>
	</div>
{/if}
