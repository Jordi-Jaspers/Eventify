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
 * Format a date string with time in ISO-like 24h format (e.g., "2024-01-15 14:30")
 */
export function formatDateTime(dateString: string | undefined): string {
	if (!dateString) return 'N/A';

	try {
		const date: Date = new Date(dateString);
		const year: number = date.getFullYear();
		const month: string = String(date.getMonth() + 1).padStart(2, '0');
		const day: string = String(date.getDate()).padStart(2, '0');
		const hours: string = String(date.getHours()).padStart(2, '0');
		const minutes: string = String(date.getMinutes()).padStart(2, '0');
		return `${year}-${month}-${day} ${hours}:${minutes}`;
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
 * Extended version that includes weeks and months for longer time periods.
 */
export function formatRelativeTime(isoString: string | null | undefined): string {
	if (!isoString) return 'Never';
	
	const date: Date = new Date(isoString);
	const now: Date = new Date();
	const diffMs: number = now.getTime() - date.getTime();
	const diffMins: number = Math.floor(diffMs / 60000);

	if (diffMins < 1) return 'Just now';
	if (diffMins < 60) return `${diffMins} min ago`;

	const diffHours: number = Math.floor(diffMins / 60);
	if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;

	const diffDays: number = Math.floor(diffHours / 24);
	if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;

	const diffWeeks: number = Math.floor(diffDays / 7);
	if (diffWeeks < 4) return `${diffWeeks} week${diffWeeks > 1 ? 's' : ''} ago`;

	const diffMonths: number = Math.floor(diffDays / 30);
	return `${diffMonths} month${diffMonths > 1 ? 's' : ''} ago`;
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
