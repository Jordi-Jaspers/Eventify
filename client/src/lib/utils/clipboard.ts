import { toast } from 'svelte-sonner';

/**
 * Copies text to clipboard with optional error toast
 * @param text - The text to copy
 * @param errorMessage - Toast message on error (default: 'Failed to copy to clipboard')
 * @returns Promise that resolves when copy is complete
 */
export async function copyToClipboard(
	text: string,
	errorMessage: string = 'Failed to copy to clipboard'
): Promise<void> {
	try {
		await navigator.clipboard.writeText(text);
	} catch (err: unknown) {
		toast.error(errorMessage);
		throw err;
	}
}
