/**
 * Formats a duration in milliseconds into a human-readable string (e.g., "2h 30m")
 */
export function formatDurationLength(start: string | number | Date, end: string | number | Date): string {
	const startTime = new Date(start).getTime();
	const endTime = new Date(end).getTime();
	const diffMs = endTime - startTime;
	
	if (diffMs < 0) return '0m';
	
	const diffMins = Math.floor(diffMs / 60000);
	const diffHours = Math.floor(diffMins / 60);
	const remainingMins = diffMins % 60;
	
	if (diffHours > 0) {
		return remainingMins > 0 ? `${diffHours}h ${remainingMins}m` : `${diffHours}h`;
	}
	return `${diffMins}m`;
}
