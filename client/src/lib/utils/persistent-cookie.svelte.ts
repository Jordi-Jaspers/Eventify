import { browser } from '$app/environment';

export class PersistentCookie<T extends string | boolean> {
	key: string = '';
	#value: T = $state<T>() as T;
	#maxAge: number;

	constructor(key: string, initialValue: T, maxAge: number = 60 * 60 * 24 * 7) {
		this.key = key;
		this.#maxAge = maxAge;

		// On client-side, read from document.cookie if available
		if (browser) {
			const cookieValue: T | null = this.readFromCookie(initialValue);
			this.#value = cookieValue !== null ? cookieValue : initialValue;
		} else {
			// On server-side, use the provided initial value
			this.#value = initialValue;
		}
	}

	get value(): T {
		return this.#value;
	}

	set value(value: T) {
		this.#value = value;
		if (browser) {
			document.cookie = `${this.key}=${this.serialize(value)}; path=/; max-age=${this.#maxAge}`;
		}
	}

	/**
	 * Read cookie value from document.cookie and parse it based on initial value type
	 */
	private readFromCookie(initialValue: T): T | null {
		if (!browser) return null;

		const cookies: string[] = document.cookie.split(';');
		for (const cookie of cookies) {
			const [name, ...valueParts] = cookie.trim().split('=');
			if (name === this.key) {
				const rawValue: string = valueParts.join('=');
				return this.deserialize(rawValue, initialValue);
			}
		}
		return null;
	}

	private serialize(value: T): string {
		return String(value);
	}

	private deserialize(rawValue: string, initialValue: T): T {
		// Determine type based on initialValue type
		if (typeof initialValue === 'boolean') {
			return (rawValue === 'true') as T;
		}
		// String type
		return rawValue as T;
	}
}
