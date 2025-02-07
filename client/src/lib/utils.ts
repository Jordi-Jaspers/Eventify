import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
	return twMerge(clsx(inputs));
}

export function sleep(ms: number) {
	return new Promise((resolve) => setTimeout(resolve, ms));
}

export function debounce<T extends (...args: any[]) => any>(func: T, waitFor: number): (...args: Parameters<T>) => void {
	let timeout: ReturnType<typeof setTimeout>;

	return (...args: Parameters<T>): void => {
		clearTimeout(timeout);
		timeout = setTimeout(() => func(...args), waitFor);
	};
}

export function toMap(obj: Record<string, any>): Map<string, any> {
	return new Map(Object.entries(obj));
}

export function formatTime(date: Date): string {
	return new Date(date).toLocaleTimeString(navigator.language, {
		hour: '2-digit',
		minute: '2-digit',
		second: '2-digit'
	});
}

export function formatDuration(startTime: Date, endTime: Date): string {
	const start = new Date(startTime);
	const end = new Date(endTime);
	const durationInSeconds = Math.round((end.getTime() - start.getTime()) / 1000);

	const days = Math.floor(durationInSeconds / (24 * 60 * 60));
	const hours = Math.floor((durationInSeconds % (24 * 60 * 60)) / (60 * 60));
	const minutes = Math.floor((durationInSeconds % (60 * 60)) / 60);
	const seconds = durationInSeconds % 60;

	const parts: string[] = [];

	if (days > 0) parts.push(`${days}d`);
	if (hours > 0) parts.push(`${hours}h`);
	if (minutes > 0) parts.push(`${minutes}m`);
	if (seconds > 0 || parts.length === 0) parts.push(`${seconds}s`);

	return parts.join(' ');
}
