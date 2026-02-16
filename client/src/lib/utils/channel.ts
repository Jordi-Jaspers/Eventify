/**
 * Channel utility functions for formatting and display
 */

import type { ChannelDetailsResponse } from '$lib/api/models';
import { toast } from 'svelte-sonner';

/**
 * Slug validation pattern (lowercase letters, numbers, and dots only)
 */
export const SLUG_PATTERN: RegExp = /^[a-z0-9]+(\.[a-z0-9]+)*$/;

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
 * Validate channel slug format
 */
export function validateSlug(value: string): { valid: boolean; error: string } {
	if (!value.trim()) {
		return { valid: false, error: 'Slug is required' };
	}
	if (!SLUG_PATTERN.test(value)) {
		return { valid: false, error: 'Use only lowercase letters, numbers, and dots' };
	}
	return { valid: true, error: '' };
}

/**
 * Copy channel slug to clipboard with toast notification
 */
export function copySlugToClipboard(channel: ChannelDetailsResponse): void {
	if (!channel.slug) return;
	navigator.clipboard
		.writeText(channel.slug)
		.then(() => {
			toast.success('Slug copied to clipboard');
		})
		.catch(() => {
			toast.error('Failed to copy slug');
		});
}

/**
 * Generate curl command for posting events to a channel
 */
export function generateCurlCommand(slug: string | undefined): string {
	return `curl -X POST https://api.eventify.dev/v1/events \\
  -H "Authorization: Bearer <YOUR_API_KEY>" \\
  -H "Content-Type: application/json" \\
  -d '{
    "slug": "${slug}",
    "severity": "INFO",
    "title": "Event Title",
    "message": "Event message here",
    "metadata": []
  }'`;
}

/**
 * Copy curl command to clipboard with toast notification
 */
export function copyCurlToClipboard(slug: string | undefined): void {
	const curlCommand: string = generateCurlCommand(slug);
	navigator.clipboard
		.writeText(curlCommand)
		.then(() => {
			toast.success('Curl command copied to clipboard');
		})
		.catch(() => {
			toast.error('Failed to copy to clipboard');
		});
}
