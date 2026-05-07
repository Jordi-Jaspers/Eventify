export interface UpdatePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export function aValidUpdatePasswordRequest(
    overrides: Partial<UpdatePasswordRequest> = {}
): UpdatePasswordRequest {
    return {
        currentPassword: 'OldPass123!',
        newPassword: 'NewPass456!',
        confirmPassword: 'NewPass456!',
        ...overrides
    };
}

export function aMismatchedUpdatePasswordRequest(): UpdatePasswordRequest {
    return aValidUpdatePasswordRequest({
        newPassword: 'NewPass456!',
        confirmPassword: 'DifferentPass789!'
    });
}

export function aWeakPasswordRequest(): UpdatePasswordRequest {
    return aValidUpdatePasswordRequest({
        newPassword: 'weak',
        confirmPassword: 'weak'
    });
}
