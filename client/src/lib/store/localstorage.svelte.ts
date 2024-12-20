import { browser } from '$app/environment';
import { toast } from 'svelte-sonner';

export class Localstorage<T> {
	key: string = '';
	#value: T = $state<T>() as T;

	constructor(key: string, initialValue: T) {
		this.key = key;
		this.#value = initialValue;

		if (browser) {
			const storedValue: string | null = localStorage.getItem(this.key);
			if (storedValue) {
				try {
					if (storedValue === 'undefined') {
						localStorage.removeItem(this.key);
						return;
					}
					this.#value = this.deserialize(storedValue);
				} catch (e) {
					toast.error('Failed to parse stored value');
				}
			} else {
				localStorage.setItem(this.key, this.serialize(this.#value));
			}
		}
	}

	get value(): T {
		return this.#value;
	}

	set value(value: T) {
		this.#value = value;
		if (browser) {
			localStorage.setItem(this.key, this.serialize(this.#value));
		}
	}

	// Determine if value needs to be parsed as JSON or used as-is
	private deserialize(value: string): T {
		if (typeof this.value === 'string' || this.isJSONString(value)) {
			return JSON.parse(value);
		}
		return value as unknown as T;
	}

	// Convert the value to a string for storage in localStorage
	private serialize(value: T): string {
		return JSON.stringify(value);
	}

	/**
	 * Check if the value is a valid JSON string.
	 * @param value The value to check
	 */
	private isJSONString(value: string): boolean {
		try {
			JSON.parse(value);
			return true;
		} catch {
			return false;
		}
	}

	/**
	 * Reset the store value to the initial value and clear localStorage
	 * @param initialValue The initial value to reset to
	 */
	reset(initialValue: T): void {
		this.#value = initialValue;
		localStorage.removeItem(this.key);
	}
}
