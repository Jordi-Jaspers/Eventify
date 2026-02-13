import { Localstorage } from '$lib/utils/localstorage.svelte';
import { getLatestVersion } from '$lib/data/changelog';

const STORAGE_KEY: string = 'eventify_last_seen_version';

/**
 * Version tracking store using localStorage.
 * Tracks the last version the user has seen to display "new updates" indicator.
 */
class VersionStore {
	#storage: Localstorage<string> = new Localstorage<string>(STORAGE_KEY, '');
	#currentVersion: string = getLatestVersion();

	get currentVersion(): string {
		return this.#currentVersion;
	}

	get lastSeenVersion(): string {
		return this.#storage.value;
	}

	get hasNewVersion(): boolean {
		if (!this.#storage.value) return true;
		return this.#currentVersion !== this.#storage.value;
	}

	markAsSeen(): void {
		this.#storage.value = this.#currentVersion;
	}
}

export const versionStore: VersionStore = new VersionStore();
