/**
 * Channel utility functions for formatting and display
 */

import { truncateText } from './string';

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
 * @deprecated Use truncateText from utils/string.ts instead
 */
export function truncateDescription(
	description: string | undefined | null,
	maxLength: number = 80
): string {
	return truncateText(description, maxLength, 'No description');
}
