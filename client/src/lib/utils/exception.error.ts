export class ApiException extends Error {
	public readonly status: number;
	public readonly code?: string;

	constructor(status: number = 500, message: string, code?: string) {
		super(message);
		this.name = 'Exception';
		this.status = status;
		this.code = code;
	}

	static fromResponse(response: Response, error?: any): ApiException {
		this.logResponse(response, error);
		if (response.status >= 500) {
			return new ApiException(
				response.status,
				'An error occurred while processing your request. Please try again later.',
				'INTERNAL_SERVER_ERROR'
			);
		}

		if (response.status >= 400) {
			// Validation error
			if ('errors' in error) {
				const exception: ValidationException = error;
				return new ApiException(
					response.status,
					exception.errors.map((error: ValidationField) => `${error.code}`).join('\n'),
					'VALIDATION_EXCEPTION'
				);
			}

			// API error
			if ('apiErrorReason' in error && error.apiErrorReason !== null) {
				const exception: Exception = error;
				return new ApiException(response.status, exception.apiErrorReason, exception.apiErrorCode);
			}

			// General error
			if ('errorMessage' in error && error.errorMessage !== null) {
				const exception: Exception = error;
				return new ApiException(response.status, exception.errorMessage, 'GENERAL_EXCEPTION');
			}
		}

		return new ApiException(response.status, 'Something went wrong processing your request.', 'UNHANDLED_EXCEPTION');
	}

	static logResponse(response: Response, error?: any): void {
		let consoleMessage = 'Response Log:\n';
		consoleMessage += '  Request:\n';
		consoleMessage += '    Method: ' + (error?.method || 'N/A') + '\n';
		consoleMessage += '    URI: ' + (error?.uri || 'N/A') + '\n';
		consoleMessage += '  Status:\n';
		consoleMessage += '    Code: ' + (error?.statusCode || response.status) + '\n';
		consoleMessage += '    Message: ' + (error?.statusMessage || response.statusText) + '\n';
		consoleMessage += '  Headers:\n';

		response.headers.forEach((value, key) => {
			consoleMessage += '    ' + key + ': ' + value + '\n';
		});

		consoleMessage += '  Body:\n';
		consoleMessage += JSON.stringify(error, null, 2) + '\n';
		console.error(consoleMessage);
	}
}
