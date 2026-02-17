/**
 * Generic inline edit state and service.
 * Reusable utility for managing inline editing logic.
 */

export interface InlineEditState {
	editing: boolean;
	saving: boolean;
	tempValue: string;
}

export interface InlineEditService {
	state: InlineEditState;
	startEdit: (currentValue: string) => void;
	cancel: () => void;
	updateTempValue: (value: string) => void;
	save: (onSave: (newValue: string) => Promise<void>) => Promise<void>;
	handleKeydown: (event: KeyboardEvent, onSave: (newValue: string) => Promise<void>) => void;
}

/**
 * Create a reusable inline edit service for a field.
 * Manages edit state, temp values, and save logic.
 */
export function createInlineEditService(): InlineEditService {
	let state: InlineEditState = $state({
		editing: false,
		saving: false,
		tempValue: ''
	});

	function startEdit(currentValue: string): void {
		state = {
			editing: true,
			saving: false,
			tempValue: currentValue || ''
		};
	}

	function cancel(): void {
		state = {
			editing: false,
			saving: false,
			tempValue: ''
		};
	}

	function updateTempValue(value: string): void {
		state.tempValue = value;
	}

	async function save(onSave: (newValue: string) => Promise<void>): Promise<void> {
		if (state.saving) return;

		state.saving = true;
		try {
			await onSave(state.tempValue.trim());
			cancel();
		} catch (error) {
			// Error already handled by onSave callback
			state.saving = false;
			throw error;
		}
	}

	function handleKeydown(
		event: KeyboardEvent,
		onSave: (newValue: string) => Promise<void>
	): void {
		if (event.key === 'Enter' && !(event.target instanceof HTMLTextAreaElement)) {
			event.preventDefault();
			save(onSave);
		} else if (event.key === 'Escape') {
			cancel();
		}
	}

	return {
		get state() {
			return state;
		},
		startEdit,
		cancel,
		updateTempValue,
		save,
		handleKeydown
	};
}
