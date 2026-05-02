<script lang="ts">
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import PublicNavbar from '$lib/components/layout/PublicNavbar.svelte';
	import PublicFooter from '$lib/components/layout/PublicFooter.svelte';
	import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Check } from '@lucide/svelte';

	interface PricingTier {
		name: string;
		price: string;
		period: string;
		description: string;
		features: string[];
		cta: string;
		ctaHref: string;
		highlighted: boolean;
		popular: boolean;
	}

	const tiers: PricingTier[] = [
		{
			name: 'Free',
			price: '$0',
			period: '/mo',
			description: 'Perfect for personal projects and getting started.',
			features: [
				'10,000 events/month',
				'3 channels',
				'90-day retention',
				'1 API key'
			],
			cta: 'Start Free',
			ctaHref: CLIENT_ROUTES.REGISTER_PAGE.path,
			highlighted: false,
			popular: false
		},
		{
			name: 'Pro',
			price: '$9',
			period: '/mo',
			description: 'For teams that need more power and flexibility.',
			features: [
				'Unlimited events',
				'Unlimited channels',
				'Up to 5-year retention',
				'Unlimited API keys'
			],
			cta: 'Get Started',
			ctaHref: CLIENT_ROUTES.REGISTER_PAGE.path,
			highlighted: true,
			popular: true
		},
		{
			name: 'Enterprise',
			price: 'Custom',
			period: '',
			description: 'For large organizations with advanced requirements.',
			features: [
				'Everything in Pro',
				'Multi-tenant organizations',
				'Unlimited team members',
				'SSO / SAML',
				'Priority support'
			],
			cta: 'Contact Us',
			ctaHref: 'mailto:sales@eventify.dev',
			highlighted: false,
			popular: false
		}
	];


</script>

<svelte:head>
	<title>Pricing - Eventify</title>
	<meta name="description" content="Simple, transparent pricing for every team. Start free, scale as you grow." />
</svelte:head>

<!-- Navigation Bar -->
<PublicNavbar activePage="pricing" />

<!-- Main Content -->
<main class="overflow-x-hidden">
	<section class="pt-32 pb-20 px-4 sm:px-6 lg:px-8">
		<div class="container mx-auto max-w-7xl">
			<!-- Header -->
			<div class="text-center mb-16 animate-fade-in-up">
				<h1 class="text-4xl sm:text-5xl font-bold mb-4 text-primary">Simple, Transparent Pricing</h1>
				<p class="text-lg text-muted-foreground max-w-2xl mx-auto">
					Start free, scale as you grow. No hidden fees, no surprises.
				</p>
			</div>

			<!-- Pricing Grid -->
			<div class="grid md:grid-cols-3 gap-8 items-stretch animate-fade-in-up">
				{#each tiers as tier}
					<div
						data-testid="pricing-tier"
						class="flex flex-col"
					>
						<Card
							class="flex flex-col h-full {tier.highlighted
								? 'bg-card/50 backdrop-blur-xl border border-primary/50 shadow-xl shadow-primary/10'
								: 'bg-card/50 backdrop-blur-xl border border-border/50 shadow-lg'}"
						>
							<CardHeader class="pb-4">
								<div class="flex items-center justify-between mb-2">
									<CardTitle class="text-xl font-bold">{tier.name}</CardTitle>
									{#if tier.popular}
										<Badge class="text-xs">Popular</Badge>
									{/if}
								</div>
								<div class="flex items-baseline gap-1">
									<span class="text-4xl font-black text-foreground">{tier.price}</span>
									{#if tier.period}
										<span class="text-muted-foreground text-sm">{tier.period}</span>
									{/if}
								</div>
								<p class="text-sm text-muted-foreground mt-2">{tier.description}</p>
							</CardHeader>

							<CardContent class="flex-1 pb-6">
								<ul class="space-y-3">
									{#each tier.features as feature}
										<li class="flex items-center gap-3 text-sm">
											<Check class="h-4 w-4 text-primary shrink-0" />
											<span>{feature}</span>
										</li>
									{/each}
								</ul>
							</CardContent>

							<CardFooter>
								<Button
									href={tier.ctaHref}
									variant={tier.highlighted ? 'default' : 'outline'}
									class="w-full"
								>
									{tier.cta}
								</Button>
							</CardFooter>
						</Card>
					</div>
				{/each}
			</div>
		</div>
	</section>
</main>

<!-- Footer -->
<PublicFooter
	productLinks={[
		{ label: 'Features', href: '/#features' },
		{ label: 'Pricing', href: '/pricing' }
	]}
/>
