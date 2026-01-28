import { browser } from '$app/environment';

/**
 * Reactive sessionStorage wrapper using Svelte 5 runes.
 * 
 * Similar to Localstorage but uses sessionStorage (data cleared when tab closes).
 * Useful for temporary state that should persist during navigation but not across sessions.
 * 
 * @example
 * ```ts
 * const session = new Sessionstorage('my_key', { count: 0 });
 * 
 * // Reading (reactive)
 * console.log(session.value.count);
 * 
 * // Writing (auto-saves to sessionStorage)
 * session.value = { count: 1 };
 * 
 * // Reset to initial value
 * session.reset({ count: 0 });
 * ```
 */
export class Sessionstorage<T> {
	key: string;
	#value: T = $state<T>() as T;

	constructor(key: string, initialValue: T) {
		this.key = key;
		this.#value = initialValue;

		if (browser) {
			const stored: string | null = sessionStorage.getItem(this.key);
			if (stored) {
				try {
					if (stored === 'undefined') {
						sessionStorage.removeItem(this.key);
						return;
					}
					this.#value = this.deserialize(stored);
				} catch (e) {
					console.error('Failed to parse session storage value:', e);
				}
			}
		}
	}

	get value(): T {
		return this.#value;
	}

	set value(value: T) {
		this.#value = value;
		if (browser) {
			sessionStorage.setItem(this.key, this.serialize(value));
		}
	}

	/**
	 * Update a partial value (useful for objects).
	 * Merges the partial with current value and saves.
	 */
	update(partial: Partial<T>): void {
		if (typeof this.#value === 'object' && this.#value !== null) {
			this.value = { ...this.#value, ...partial };
		}
	}

	/**
	 * Reset the store value to the initial value and clear sessionStorage.
	 */
	reset(initialValue: T): void {
		this.#value = initialValue;
		if (browser) {
			sessionStorage.removeItem(this.key);
		}
	}

	private deserialize(value: string): T {
		return JSON.parse(value) as T;
	}

	private serialize(value: T): string {
		return JSON.stringify(value);
	}
}
