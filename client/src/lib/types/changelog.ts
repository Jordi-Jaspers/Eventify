export interface ChangelogEntry {
	version: string; // "1.0.0"
	date: string; // "2026-02-13"
	features?: string[]; // New features
	improvements?: string[]; // Enhancements
	fixes?: string[]; // Bug fixes
}
