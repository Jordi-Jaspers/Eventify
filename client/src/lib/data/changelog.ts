import type { ChangelogEntry } from '$lib/types/changelog';

export const changelog: ChangelogEntry[] = [
	{
		version: '0.0.9',
		date: '2026-02-17',
		features: [
			'Channel slugs: human-readable identifiers like "myapp.prod.errors" for channels',
			'Channel Details Sheet with hero header, inline editing, and quick actions',
			'Stale channel detection: amber "Stale" badge for channels with no events in 7+ days',
			'Last Activity column with relative time display and sorting',
			'Send Events help modal with cURL examples for single and batch ingestion',
			'API channel creation via POST /api/v1/channels using API key authentication',
			'Event routing by slug: send events using channelSlug instead of channelId'
		],
		improvements: [
			'Click anywhere on channel row to open details',
			'Copy Slug and Copy cURL quick actions in channel details',
			'Slug column displayed in channel tables',
			'Reusable InlineEditableText, InfoCard, and CodeBlockWithCopy components',
			'Consolidated channel creation validation across Web UI and API'
		],
		fixes: [
			'Scheduled jobs now run immediately on startup (stale detection, event retention)',
			'Resolved Svelte 5 reactivity warnings in Monitor EventsList',
			'Existing channels backfilled with last activity timestamp'
		]
	},
	{
		version: '0.0.8',
		date: '2026-02-13',
		features: [
			'Environment-aware branding with DEV/TST badges on logo and favicon',
			'What\'s New changelog page with version indicator',
			'Component Playbook moved to sidebar footer (dev environments)'
		],
		improvements: [
			'Centralized environment detection module',
			'Single PUBLIC_ENVIRONMENT variable controls all env features',
			'PulseIndicator component for consistent animated status dots'
		],
		fixes: [
			'Organization API keys no longer incorrectly enforce personal quota',
			'Monitor page now loads watchlist\'s saved default filters on first visit'
		]
	},
	{
		version: '0.0.7',
		date: '2026-02-12',
		features: [
			'Duration details modal with event drill-down from timeline',
			'MiniTimeline navigation with prev/next window cursors',
			'Event search API for user and organization channels',
			'Dashboard stats cards: Events Today, Active Channels, Error Rate, Last Event'
		],
		improvements: [
			'StatCard extracted as shared UI component for cross-dashboard reuse',
			'Custom favicon with Eventify brand colors'
		],
		fixes: [
			'Channel search status filter no longer causes server error',
			'Secured user channel endpoints with @PreAuthorize for defense-in-depth'
		]
	},
	{
		version: '0.0.6',
		date: '2026-01-28',
		features: [
			'Organization watchlists with role-based access (Owner/Admin manage, Member view)',
			'Watchlist builder UI for organizations',
			'Timeline monitoring page with live auto-refresh mode',
			'Consolidated dashboard timeline with per-channel breakdown',
			'Time range presets and custom date selection',
			'URL sharing for monitor configurations'
		],
		improvements: [
			'Abstract watchlist service for DRY code between user and org',
			'Monitor service layer extraction for cleaner components',
			'Session storage for filter persistence'
		]
	},
	{
		version: '0.0.5',
		date: '2026-01-25',
		features: [
			'Personal watchlist CRUD with channel groups and filter rules',
			'Timeline aggregation API with severity-based coloring',
			'Watchlist list page with search and sorting'
		],
		improvements: [
			'JSONB configuration storage (eliminated join tables)',
			'Sweep-line algorithm for efficient timeline building',
			'Professional dark mode palette with teal accents'
		]
	},
	{
		version: '0.0.4',
		date: '2026-01-21',
		features: [
			'Real-time event ingestion API (POST /v1/events)',
			'Batch event ingestion up to 100 events per request',
			'Event severity levels: OK, Warning, Critical',
			'Monthly quota enforcement (1000 events/user) with rate limit headers',
			'Automatic event retention cleanup job'
		],
		improvements: [
			'TimescaleDB hypertable with 7-day chunks and compression',
			'Request-scoped caching to reduce duplicate DB queries',
			'Pessimistic locking for quota updates'
		]
	},
	{
		version: '0.0.3',
		date: '2026-01-16',
		features: [
			'Event channel management for personal and organization use',
			'Channel status lifecycle: Active, Paused, Pending Deletion',
			'Configurable data retention settings (user and organization level)',
			'API key authentication for event endpoints (X-Api-Key header)',
			'Channel access validation for API keys'
		],
		improvements: [
			'Background job for automatic channel cleanup',
			'Consistent Job+Service pattern for all scheduled tasks'
		]
	},
	{
		version: '0.0.2',
		date: '2026-01-09',
		features: [
			'Personal API keys with secure generation (5-key limit)',
			'Organization API keys for shared team access',
			'API key admin dashboard with platform-wide management',
			'User settings page with API key management',
			'Event quota tracking with monthly reset'
		],
		improvements: [
			'BCrypt hashing for API key security',
			'Key usage tracking and expiration support',
			'Masked key display (evt_******xxxx)'
		],
		fixes: [
			'Token refresh bypass that redirected to login prematurely'
		]
	},
	{
		version: '0.0.1',
		date: '2026-01-01',
		features: [
			'Multi-tenant organization support with role-based access',
			'Organization provisioning by global admins',
			'Member management: add, remove, update roles, transfer ownership',
			'Organization switcher for multi-org users',
			'Admin dashboard with platform statistics and 30-day growth charts',
			'User management page with lock/unlock and role changes',
			'Force password reset security action'
		],
		improvements: [
			'Collapsible sidebar navigation with glassmorphism design',
			'DataTable component with server-side pagination and filtering',
			'OpenAPI schema annotations for better API documentation'
		],
		fixes: [
			'Auth redirect loop when cookies deleted but localStorage persists',
			'User organizations not showing in details response'
		]
	},
	{
		version: '0.0.0',
		date: '2025-12-01',
		features: [
			'User registration with email verification',
			'JWT authentication with access and refresh tokens',
			'Password reset flow with branded email templates',
			'User profile page with inline editing',
			'Global admin bootstrap from environment variables'
		],
		improvements: [
			'Production-ready auth flow with automatic token refresh',
			'Responsive design for all screen sizes',
			'Animated gradient backgrounds on public pages'
		]
	}
];

export function getLatestVersion(): string {
	return changelog[0]?.version ?? '0.0.0';
}
