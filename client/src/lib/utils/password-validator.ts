export interface PasswordRule {
    id: string;
    label: string;
    satisfied: boolean;
    errorMessage: string;
}

export interface PasswordValidationResult {
    isValid: boolean;
    score: number;
    maxScore: number;
    rules: PasswordRule[];
    strength: 'weak' | 'fair' | 'good' | 'strong';
}

export function validatePassword(password: string): PasswordValidationResult {
    const rules: PasswordRule[] = [
        {
            id: 'length',
            label: '8-100 characters',
            satisfied: password.length >= 8 && password.length <= 100,
            errorMessage: 'Password must be 8-100 characters'
        },
        {
            id: 'uppercase',
            label: 'At least one uppercase letter',
            satisfied: /[A-Z]/.test(password),
            errorMessage: 'Password must contain at least one uppercase letter'
        },
        {
            id: 'lowercase',
            label: 'At least one lowercase letter',
            satisfied: /[a-z]/.test(password),
            errorMessage: 'Password must contain at least one lowercase letter'
        },
        {
            id: 'digit',
            label: 'At least one digit',
            satisfied: /\d/.test(password),
            errorMessage: 'Password must contain at least one digit'
        },
        {
            id: 'special',
            label: 'At least one special character',
            satisfied: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password),
            errorMessage: 'Password must contain at least one special character'
        },
        {
            id: 'no-whitespace',
            label: 'No whitespace allowed',
            satisfied: !/\s/.test(password),
            errorMessage: 'Password cannot contain whitespace'
        }
    ];

    const score: number = rules.filter((rule: PasswordRule): boolean => rule.satisfied).length;
    const maxScore: number = rules.length;
    const isValid: boolean = score === maxScore;

    let strength: 'weak' | 'fair' | 'good' | 'strong';
    if (score <= 2) {
        strength = 'weak';
    } else if (score <= 4) {
        strength = 'fair';
    } else if (score < maxScore) {
        strength = 'good';
    } else {
        strength = 'strong';
    }

    return {
        isValid,
        score,
        maxScore,
        rules,
        strength
    };
}

export function validateEmail(email: string): boolean {
    const emailRegex: RegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

export function validateRequired(value: string): boolean {
    return value.trim().length > 0;
}

export function validateName(name: string): boolean {
    return name.trim().length >= 1 && name.trim().length <= 255;
}
