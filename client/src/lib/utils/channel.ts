/**
 * Channel utility functions for formatting and display
 */

/**
 * Get badge variant for channel status
 */
export function getChannelStatusVariant(
	status: string | undefined
): 'success' | 'secondary' {
	return status === 'ACTIVE' ? 'success' : 'secondary';
}

/**
 * Get label for channel status
 */
export function getChannelStatusLabel(status: string | undefined): string {
	return status === 'ACTIVE' ? 'Active' : 'Paused';
}

/**
 * Truncate description text with ellipsis
 */
export function truncateDescription(
	description: string | undefined | null,
	maxLength: number = 80
): string {
	if (!description) return 'No description';
	if (description.length <= maxLength) return description;
	return `${description.substring(0, maxLength)}...`;
}
