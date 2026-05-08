<!--
  PublicFooter Component

  Footer for public-facing pages (landing, pricing, etc.).

  Props:
  - productLinks: optional array of { label, href } to override product section links

  Usage:
  <PublicFooter />
  <PublicFooter productLinks={[{ label: 'Features', href: '/#features' }, { label: 'Pricing', href: '/pricing' }]} />
-->
<script lang="ts">
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import AppLogo from '$lib/components/layout/AppLogo.svelte';

	interface FooterLink {
		label: string;
		href?: string;
		onclick?: () => void;
	}

	interface Props {
		productLinks?: FooterLink[];
	}

	let { productLinks = [] }: Props = $props();
</script>

<footer class="border-t border-border/50 bg-muted/30 py-12 px-4 sm:px-6 lg:px-8">
	<div class="container mx-auto max-w-7xl">
		<div class="grid md:grid-cols-4 gap-8 mb-8">
			<!-- Brand -->
			<div class="md:col-span-2">
				<div class="mb-4">
					<AppLogo size="small" />
				</div>
				<p class="text-sm text-muted-foreground max-w-sm">
					Real-time event monitoring for modern teams. Track, analyze, and act on events across
					your distributed systems.
				</p>
			</div>

			<!-- Product Links -->
			<div>
				<h4 class="font-semibold mb-4">Product</h4>
				<ul class="space-y-2 text-sm">
					{#each productLinks as link}
						<li>
							<Button
								variant="link"
								href={link.href}
								onclick={link.onclick}
								class="text-muted-foreground hover:text-primary p-0 h-auto font-normal"
							>
								{link.label}
							</Button>
						</li>
					{/each}
				</ul>
			</div>

			<!-- Account Links -->
			<div>
				<h4 class="font-semibold mb-4">Account</h4>
				<ul class="space-y-2 text-sm">
					<li>
						<Button
							href={CLIENT_ROUTES.LOGIN_PAGE.path}
							variant="link"
							class="text-muted-foreground hover:text-primary p-0 h-auto font-normal"
						>
							Login
						</Button>
					</li>
					<li>
						<Button
							href={CLIENT_ROUTES.REGISTER_PAGE.path}
							variant="link"
							class="text-muted-foreground hover:text-primary p-0 h-auto font-normal"
						>
							Register
						</Button>
					</li>
				</ul>
			</div>
		</div>

		<!-- Copyright -->
		<div class="pt-8 border-t border-border/50">
			<p class="text-sm text-center text-muted-foreground">
				© {new Date().getFullYear()} Eventify. All rights reserved.
			</p>
		</div>
	</div>
</footer>
