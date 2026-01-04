import type {
    ApiErrorResponseResource,
    ErrorResponseResource,
    ValidationErrorResource,
    ValidationErrorResponseResource
} from "$lib/api/models.ts";

/**
 * Result of error handling containing user-friendly message and optional validation errors
 */
export interface ErrorHandlerResult {
	message: string;
	validationErrors?: ValidationErrorResource[];
	traceId?: string;
    txId?: string;
	apiErrorCode?: string;
}

/**
 * Type guard to check if error is ErrorResponseResource
 */
function isErrorResponseResource(error: unknown): error is ErrorResponseResource {
	return (
		typeof error === 'object' &&
		error !== null &&
		'errorMessage' in error &&
		typeof (error as ErrorResponseResource).errorMessage === 'string'
	);
}

/**
 * Type guard to check if error is ApiErrorResponseResource
 */
function isApiErrorResponseResource(error: unknown): error is ApiErrorResponseResource {
	return (
		isErrorResponseResource(error) &&
		'apiErrorCode' in error &&
		typeof (error as ApiErrorResponseResource).apiErrorCode === 'string'
	);
}

/**
 * Type guard to check if error is ValidationErrorResponseResource
 */
function isValidationErrorResponseResource(
	error: unknown
): error is ValidationErrorResponseResource {
	return (
		isErrorResponseResource(error) &&
		'errors' in error &&
		Array.isArray((error as ValidationErrorResponseResource).errors)
	);
}

/**
 * Handles backend error responses and returns user-friendly error information
 *
 * @param error - The error object from the backend or any unknown error
 * @param fallbackMessage - Optional fallback message if error cannot be parsed (default: 'An unexpected error occurred')
 * @returns ErrorHandlerResult with user-friendly message and optional validation errors
 */
export function handleError(error: unknown, fallbackMessage: string = 'An unexpected error occurred'): ErrorHandlerResult {
	// Handle ValidationErrorResponseResource (validation errors)
	if (isValidationErrorResponseResource(error)) {
		const validationError: ValidationErrorResponseResource = error;
		return {
			message: validationError.errorMessage || 'Validation failed',
			validationErrors: validationError.errors,
			traceId: validationError.traceId,
            txId: validationError.txId
		};
	}

	// Handle ApiErrorResponseResource (API-specific errors)
	if (isApiErrorResponseResource(error)) {
		const apiError: ApiErrorResponseResource = error;
		const message: string =
			apiError.apiErrorReason ||
			apiError.errorMessage ||
			`API Error: ${apiError.apiErrorCode}`;
		return {
			message,
			apiErrorCode: apiError.apiErrorCode,
			traceId: apiError.traceId,
            txId: apiError.txId
		};
	}

	// Handle generic ErrorResponseResource
	if (isErrorResponseResource(error)) {
		const errorResponse: ErrorResponseResource = error;
		return {
			message: errorResponse.errorMessage || fallbackMessage,
			traceId: errorResponse.traceId,
            txId: errorResponse.txId
		};
	}

	// Handle standard JavaScript Error objects
	if (error instanceof Error) {
		return {
			message: error.message || fallbackMessage
		};
	}

	// Handle string errors
	if (typeof error === 'string') {
		return {
			message: error
		};
	}

	// Fallback for unknown error types
	return {
		message: fallbackMessage
	};
}

/**
 * Formats validation errors into a human-readable string
 *
 * @param validationErrors - Array of validation errors
 * @returns Formatted string with all validation errors
 */
export function formatValidationErrors(validationErrors: ValidationErrorResource[]): string {
	return validationErrors
		.map((err: ValidationErrorResource) => `${err.field}: ${err.code}`)
		.join('\n');
}

/**
 * Converts validation errors into a field-keyed object for easy lookup
 *
 * @param validationErrors - Array of validation errors
 * @returns Object with field names as keys and error codes as values
 */
export function getValidationErrorMap(
	validationErrors: ValidationErrorResource[]
): Record<string, string> {
	const errorMap: Record<string, string> = {};
	validationErrors.forEach((err: ValidationErrorResource) => {
		if (err.field && err.code) {
			errorMap[err.field] = err.code;
		}
	});
	return errorMap;
}
