/**
 * Format a date string as a readable date (e.g., "Jan 15, 2024")
 */
export function formatDate(dateString: string): string {
	const date: Date = new Date(dateString);
	return date.toLocaleDateString('en-US', {
		year: 'numeric',
		month: 'short',
		day: 'numeric'
	});
}

/**
 * Format a date string with time (e.g., "Jan 15, 2024, 2:30 PM")
 */
export function formatDateTime(dateString: string | undefined): string {
	if (!dateString) return 'N/A';

	try {
		const date: Date = new Date(dateString);
		return new Intl.DateTimeFormat('en-US', {
			dateStyle: 'medium',
			timeStyle: 'short'
		}).format(date);
	} catch {
		return 'Invalid date';
	}
}

/**
 * Format a date string as a relative time (e.g., "2 days ago")
 */
export function formatRelativeDate(dateString: string): string {
	const date: Date = new Date(dateString);
	const now: Date = new Date();
	const diffMs: number = now.getTime() - date.getTime();
	const diffDays: number = Math.floor(diffMs / (1000 * 60 * 60 * 24));

	if (diffDays === 0) return 'Today';
	if (diffDays === 1) return 'Yesterday';
	if (diffDays < 7) return `${diffDays} days ago`;
	if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
	if (diffDays < 365) return `${Math.floor(diffDays / 30)} months ago`;
	return `${Math.floor(diffDays / 365)} years ago`;
}

/**
 * Format a date string as a relative time with high precision for recent events
 * (e.g., "Just now", "5 min ago", "2 hours ago")
 */
export function formatRelativeTime(isoString: string | null | undefined): string {
	if (!isoString) return 'Never';
	const date = new Date(isoString);
	const now = new Date();
	const diffMs = now.getTime() - date.getTime();
	const diffMins = Math.floor(diffMs / 60000);

	if (diffMins < 1) return 'Just now';
	if (diffMins < 60) return `${diffMins} min ago`;

	const diffHours = Math.floor(diffMins / 60);
	if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;

	const diffDays = Math.floor(diffHours / 24);
	return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
}

/**
 * Format a date string as time only (e.g., "14:30")
 */
export function formatTime(dateString: string): string {
	const date = new Date(dateString);
	return date.toLocaleTimeString('en-US', {
		hour: '2-digit',
		minute: '2-digit',
		hour12: false
	});
}
