import { browser } from '$app/environment';

export class PersistentCookie<T> {
	key: string = '';
	#value: T = $state<T>() as T;
	#maxAge: number;

	constructor(key: string, initialValue: T, maxAge: number = 60 * 60 * 24 * 7) {
		this.key = key;
		this.#value = initialValue;
		this.#maxAge = maxAge;

		// On client-side init, trust the passed initialValue (from SSR) first if provided,
		// but if we are purely client-side or want to sync, we can read from document.cookie.
		// However, for hydration matching, we usually want to start with what the server sent.
		// If initialValue is provided, we assume it matches the cookie.
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

	private serialize(value: T): string {
		return String(value);
	}
}
