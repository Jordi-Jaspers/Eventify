---
epic: "LAUNCH"
title: "Landing Page Polish"
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
**I want** a clear, accurate landing page\
**So that** I understand what Eventify does and want to sign up\

## 2. Business Context & Value
The landing page is the first impression. The curl example is wrong (wrong endpoint, wrong fields, wrong auth header), the "How It Works" steps are generic, and the hero copy could be sharper. Fixing these removes friction for developer adoption.

## 3. Acceptance Criteria
* [ ] **Correct curl example**: Shows actual API
    * Given the landing page code example section
    * When a developer reads the curl command
    * Then it shows `POST /v1/external/event` with `X-API-Key` header and `{slug, severity, title, message, metadata}` payload using a deployment event example
* [ ] **Sharper hero copy**:
    * Given a visitor lands on the page
    * When they see the hero section
    * Then the headline reads "Event monitoring for developers. Simple API. Real-time insights." (or similar concise tagline)
* [ ] **Concrete "How It Works" steps**:
    * Given the How It Works section
    * When a visitor reads the 3 steps
    * Then they see: 1) "Create a Channel" — organize by service/env/team, 2) "Send Events" — one API call, any language, 3) "Monitor & React" — live dashboards, severity tracking, trends
* [ ] **Pricing link in navbar**:
    * Given the landing page navbar
    * When a visitor looks for pricing
    * Then there is a "Pricing" link navigating to `/pricing`

## 4. Technical Requirements
* **API Changes**: N/A
* **Database**: N/A
* **Security**: N/A
* **Performance**: N/A

## 5. Design & UI/UX
Keep existing layout, glassmorphism, and animations. Only change text content, curl example, and add navbar link. No structural redesign.

## 6. Implementation Notes
- **File:** `client/src/routes/(public)/+page.svelte` (690 lines)
- Curl example is at lines 46-53 and 569-576
- How It Works section at lines 446-528
- Hero section near top of file
- Navbar links are in the same file (fixed header)
- Use deployment event example: slug "deployments", severity "OK", title "Deployment Successful"
- Auth header: `X-API-Key: ev_live_abc123` (not Bearer)

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `client/src/routes/(public)/+page.svelte` | Fix curl, update hero copy, update How It Works, add Pricing nav link | 46-53, 446-528, 569-576, navbar section |
