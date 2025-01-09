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
