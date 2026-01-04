import { updateUserDetails } from './UserController';
import type { UserDetailsResponse } from '$lib/api/models';
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';
import { authStore, currentUser } from '$lib/stores/auth';
import { get } from 'svelte/store';

export type EditableField = 'firstName' | 'lastName';

interface FieldEditState {
    editing: boolean;
    saving: boolean;
    tempValue: string;
}

/**
 * Service for managing profile editing state and operations.
 * Encapsulates inline editing logic for user profile fields.
 */
export function createProfileService() {
    // Track edit state for each field
    let firstNameState: FieldEditState = $state({ editing: false, saving: false, tempValue: '' });
    let lastNameState: FieldEditState = $state({ editing: false, saving: false, tempValue: '' });

    /**
     * Get the current user data from the auth store
     */
    function getUserData(): UserDetailsResponse | null {
        return get(currentUser);
    }

    /**
     * Start editing a field - captures current value
     */
    function startEdit(field: EditableField): void {
        const userData = getUserData();
        if (!userData) return;

        if (field === 'firstName') {
            firstNameState = {
                editing: true,
                saving: false,
                tempValue: userData.firstName || ''
            };
        } else {
            lastNameState = {
                editing: true,
                saving: false,
                tempValue: userData.lastName || ''
            };
        }
    }

    /**
     * Cancel editing - resets temp value and exits edit mode
     */
    function cancelEdit(field: EditableField): void {
        if (field === 'firstName') {
            firstNameState = { editing: false, saving: false, tempValue: '' };
        } else {
            lastNameState = { editing: false, saving: false, tempValue: '' };
        }
    }

    /**
     * Update temp value while editing
     */
    function updateTempValue(field: EditableField, value: string): void {
        if (field === 'firstName') {
            firstNameState.tempValue = value;
        } else {
            lastNameState.tempValue = value;
        }
    }

    /**
     * Save a field - calls API and updates auth store
     */
    async function saveField(field: EditableField): Promise<void> {
        const userData = getUserData();
        if (!userData) return;

        const state = field === 'firstName' ? firstNameState : lastNameState;
        if (state.saving) return;

        const originalValue = field === 'firstName' 
            ? (userData.firstName || '') 
            : (userData.lastName || '');
        
        const newValue = state.tempValue.trim();

        // No change - just exit edit mode
        if (newValue === originalValue) {
            cancelEdit(field);
            return;
        }

        // Set saving state
        if (field === 'firstName') {
            firstNameState.saving = true;
        } else {
            lastNameState.saving = true;
        }

        try {
            const firstName = field === 'firstName' ? newValue : (userData.firstName || '');
            const lastName = field === 'lastName' ? newValue : (userData.lastName || '');

            const updatedUser: UserDetailsResponse = await updateUserDetails(firstName, lastName);
            authStore.setUser(updatedUser);
            
            cancelEdit(field);
            
            const fieldLabel = field === 'firstName' ? 'First name' : 'Last name';
            toast.success(`${fieldLabel} updated`);
        } catch (err: unknown) {
            const fieldLabel = field === 'firstName' ? 'first name' : 'last name';
            const { message }: { message: string } = handleError(err, `Failed to update ${fieldLabel}`);
            toast.error(message);
            
            // Reset temp value to original
            if (field === 'firstName') {
                firstNameState.tempValue = originalValue;
                firstNameState.saving = false;
            } else {
                lastNameState.tempValue = originalValue;
                lastNameState.saving = false;
            }
        }
    }

    /**
     * Handle keyboard events for inline editing
     */
    function handleKeydown(field: EditableField, event: KeyboardEvent): void {
        if (event.key === 'Enter') {
            event.preventDefault();
            saveField(field);
        } else if (event.key === 'Escape') {
            cancelEdit(field);
        }
    }

    return {
        // State getters
        get firstNameState() { return firstNameState; },
        get lastNameState() { return lastNameState; },
        
        // Actions
        startEdit,
        cancelEdit,
        updateTempValue,
        saveField,
        handleKeydown
    };
}

export type ProfileService = ReturnType<typeof createProfileService>;
