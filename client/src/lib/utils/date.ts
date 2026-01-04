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
