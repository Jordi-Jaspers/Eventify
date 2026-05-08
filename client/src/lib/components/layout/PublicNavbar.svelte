<!--
  PublicNavbar Component

  Navigation bar for public-facing pages (landing, pricing, etc.).
  Handles scroll-based styling, mobile menu, and auth-aware links.

  Props:
  - activePage: 'landing' | 'pricing' — highlights the current page link

  Usage:
  <PublicNavbar activePage="landing" />
-->
<script lang="ts">
	import { isAuthenticated } from '$lib/stores/auth';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import AppLogo from '$lib/components/layout/AppLogo.svelte';
	import { Menu, X } from '@lucide/svelte';
	import { onMount } from 'svelte';

	interface Props {
		activePage?: 'landing' | 'pricing';
	}

	let { activePage = 'landing' }: Props = $props();

	let mobileMenuOpen: boolean = $state(false);
	let scrolled: boolean = $state(false);

	const pricingVariant = $derived(activePage === 'pricing' ? 'secondary' : 'outline');
	const pricingMobileClass = $derived(
		activePage === 'pricing'
			? 'w-full justify-start px-4 py-6 text-lg font-medium hover:bg-muted/50 rounded-md text-primary'
			: 'w-full justify-start px-4 py-6 text-lg font-medium hover:bg-muted/50 rounded-md'
	);

	onMount(() => {
		const handleScroll = (): void => {
			scrolled = window.scrollY > 20;
		};
		window.addEventListener('scroll', handleScroll);
		return () => window.removeEventListener('scroll', handleScroll);
	});
</script>

<!-- Navigation Bar -->
<nav
	class="fixed top-0 left-0 right-0 z-50 transition-all duration-300 {scrolled
		? 'bg-card/90 backdrop-blur-xl border-b border-border/50 shadow-sm'
		: 'bg-transparent'}"
>
	<div class="container mx-auto px-4 sm:px-6 lg:px-8">
		<div class="flex items-center justify-between h-16">
			<!-- Logo -->
			<AppLogo size="small" href={CLIENT_ROUTES.LANDING_PAGE.path} />

			<!-- Desktop Navigation -->
			<div class="hidden md:flex items-center gap-3">
				{#if $isAuthenticated}
					<Button href={CLIENT_ROUTES.DASHBOARD_PAGE.path}>Dashboard</Button>
				{:else}
					<Button
						variant={pricingVariant}
						href={CLIENT_ROUTES.PRICING_PAGE.path}
						class="text-sm font-medium"
					>
						Pricing
					</Button>
					<Button href={CLIENT_ROUTES.REGISTER_PAGE.path}>Get Started</Button>
				{/if}
			</div>

			<!-- Mobile Menu Button -->
			<Button
				variant="ghost"
				size="icon"
				onclick={() => (mobileMenuOpen = !mobileMenuOpen)}
				class="md:hidden text-muted-foreground hover:text-primary"
				aria-label="Toggle menu"
				aria-expanded={mobileMenuOpen}
			>
				{#if mobileMenuOpen}
					<X class="w-6 h-6" />
				{:else}
					<Menu class="w-6 h-6" />
				{/if}
			</Button>
		</div>
	</div>
</nav>

<!-- Full-screen Mobile Menu -->
{#if mobileMenuOpen}
	<div class="fixed inset-0 z-40 md:hidden">
		<!-- Backdrop -->
		<button
			onclick={() => (mobileMenuOpen = false)}
			class="absolute inset-0 bg-background/80 backdrop-blur-sm"
			aria-label="Close menu"
		></button>

		<!-- Menu Panel -->
		<div
			class="absolute inset-x-0 top-16 bottom-0 bg-background/95 backdrop-blur-xl border-t border-border/50 animate-fade-in-up overflow-y-auto"
			role="dialog"
			aria-modal="true"
		>
			<div class="flex flex-col h-full px-6 py-8">
				<!-- Navigation Links -->
				<nav class="flex-1 space-y-2">
					{#if !$isAuthenticated}
						<Button
							variant="ghost"
							href={CLIENT_ROUTES.PRICING_PAGE.path}
							onclick={() => (mobileMenuOpen = false)}
							class={pricingMobileClass}
						>
							Pricing
						</Button>
					{/if}
				</nav>

				<!-- Auth Buttons at Bottom -->
				<div class="pt-6 border-t border-border/50 space-y-3">
					{#if $isAuthenticated}
						<Button href={CLIENT_ROUTES.DASHBOARD_PAGE.path} class="w-full h-12 text-base">
							Dashboard
						</Button>
					{:else}
						<Button href={CLIENT_ROUTES.REGISTER_PAGE.path} class="w-full h-12 text-base">
							Get Started
						</Button>
					{/if}
				</div>
			</div>
		</div>
	</div>
{/if}
