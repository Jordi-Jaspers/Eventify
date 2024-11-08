import { type Writable, writable } from 'svelte/store';

export const useAuthenticated: Writable<boolean> = writable(false);
