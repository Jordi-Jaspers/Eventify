import { formatDate as formatDateUtil } from '$lib/utils/date';

/**
 * Get the badge class for a given scope
 */
export function getScopeBadgeClass(scope: string | undefined): string {
	if (scope === 'USER') return 'bg-blue-500/10 text-blue-500 border-blue-500/20 backdrop-blur-md';
	if (scope === 'ORGANIZATION')
		return 'bg-purple-500/10 text-purple-500 border-purple-500/20 backdrop-blur-md';
	return '';
}

/**
 * Format a number with locale string
 */
export function formatNumber(num: number | undefined): string {
	if (num === undefined || num === null) return '0';
	return num.toLocaleString();
}

/**
 * Format last used date or return "Never"
 */
export function formatLastUsed(date: string | null | undefined): string {
	if (!date) return 'Never';
	return formatDateUtil(date);
}
