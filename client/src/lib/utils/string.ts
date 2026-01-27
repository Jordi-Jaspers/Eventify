/**
 * Get initials from first and last name
 */
export function getInitials(firstName: string, lastName: string): string {
	return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
}

/**
 * Truncate text with ellipsis
 */
export function truncateText(
	text: string | undefined | null,
	maxLength: number = 80,
	fallback: string = 'No description'
): string {
	if (!text) return fallback;
	if (text.length <= maxLength) return text;
	return `${text.substring(0, maxLength)}...`;
}
