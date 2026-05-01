---
epic: "LAUNCH"
title: "Pricing Page"
estimate: M
status: ready
created: 2026-05-01
depends_on: [ ]
labels: [ frontend ]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** visitor\
**I want** to see pricing tiers\
**So that** I can decide which plan fits my needs\

## 2. Business Context & Value
A pricing page is essential for conversion. Visitors need to understand what's free vs paid before signing up. Standard SaaS pattern.

## 3. Acceptance Criteria
* [ ] **Three tiers displayed**:
    * Given a visitor navigates to `/pricing`
    * When the page loads
    * Then they see Free, Pro ($9/mo), and Enterprise tiers side by side
* [ ] **Free tier details**:
    * Given the Free tier card
    * When a visitor reads it
    * Then it shows: 10,000 events/mo, 3 channels, 90-day retention, 1 API key. CTA: "Start Free" → register
* [ ] **Pro tier details**:
    * Given the Pro tier card (highlighted/recommended)
    * When a visitor reads it
    * Then it shows: Unlimited events, unlimited channels, up to 5-year retention, unlimited API keys, $9/mo. CTA: "Get Started" → register
* [ ] **Enterprise tier details**:
    * Given the Enterprise tier card
    * When a visitor reads it
    * Then it shows: Everything in Pro + multi-tenant organizations, unlimited team members, SSO/SAML, priority support. CTA: "Contact Us" → mailto or contact form
* [ ] **Consistent design**:
    * Given the pricing page
    * When compared to the landing page
    * Then it uses the same navbar, footer, glassmorphism styling, and design tokens
* [ ] **Navigation**:
    * Given any public page
    * When a visitor clicks "Pricing" in the navbar
    * Then they navigate to `/pricing`

## 4. Technical Requirements
* **API Changes**: N/A
* **Database**: N/A
* **Security**: N/A — public page
* **Performance**: N/A — static content

## 5. Design & UI/UX
Standard 3-column pricing grid. Pro tier visually highlighted (border, "Popular" badge). Cards with feature lists using checkmarks. Responsive: stacks on mobile. Match landing page glassmorphism aesthetic (`bg-card/50 backdrop-blur-xl border-border/50`).

## 6. Implementation Notes
- Create new route: `client/src/routes/(public)/pricing/+page.svelte`
- Reuse components: Button, Card, Badge from `$lib/components/ui/`
- Reuse icons from `@lucide/svelte` (Check, Zap, Building2, Shield)
- Follow landing page patterns for navbar/footer (currently inline in `+page.svelte` — may need to extract shared layout or duplicate)
- The `(public)` route group layout may already provide navbar/footer — check `client/src/routes/(public)/+layout.svelte`

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `client/src/routes/(public)/pricing/+page.svelte` | NEW — pricing page | — |
| `client/src/routes/(public)/+page.svelte` | Add Pricing link to navbar (if not done in LAUNCH-01) | navbar section |
