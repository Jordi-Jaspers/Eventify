export class Exception extends Error {
	public readonly status: number;
	public readonly type: string;
	public readonly headers?: Headers;
	public readonly throwable?: DefaultException;

	constructor(response: Response, throwable?: DefaultException) {
		super('Caught an unhandled exception');
		this.name = 'Exception';
		this.status = response.status;
		this.headers = response.headers;
		this.throwable = throwable;

		// Determine exception type and customize error message
		this.type = this.determineExceptionType(throwable);
		this.message = this.customizeErrorMessage(throwable);
	}

	private determineExceptionType(throwable?: DefaultException): string {
		if (!throwable) return 'UNKNOWN_EXCEPTION';

		if ('errors' in throwable) {
			return 'VALIDATION_EXCEPTION';
		}

		if ('apiErrorReason' in throwable && throwable.apiErrorReason) {
			return 'API_EXCEPTION';
		}

		if (throwable.errorMessage) {
			return 'GENERAL_EXCEPTION';
		}

		return 'UNKNOWN_EXCEPTION';
	}

	private customizeErrorMessage(throwable?: DefaultException): string {
		if (!throwable) return this.message;

		if ('errors' in throwable) {
			const validationException = throwable as ValidationException;
			return validationException.errors.map((error: ValidationField) => `${error.code}`).join('\n');
		}

		if ('apiErrorReason' in throwable && throwable.apiErrorReason) {
			const apiException = throwable as ApiException;
			return apiException.apiErrorReason;
		}

		return throwable.errorMessage || this.message;
	}

	private logResponse(): void {
		let consoleMessage = 'Response Log:\n';
		consoleMessage += '  Request:\n';
		consoleMessage += '    Method: ' + (this.throwable?.method || 'N/A') + '\n';
		consoleMessage += '    URI: ' + (this.throwable?.uri || 'N/A') + '\n';
		consoleMessage += '  Status:\n';
		consoleMessage += '    Code: ' + (this.throwable?.statusCode || this.status) + '\n';
		consoleMessage += '    Message: ' + (this.throwable?.statusMessage || this.message) + '\n';
		consoleMessage += '  Headers:\n';

		this.headers.forEach((value, key) => {
			consoleMessage += '    ' + key + ': ' + value + '\n';
		});

		consoleMessage += '  Body:\n';
		consoleMessage += JSON.stringify(this.throwable, null, 2) + '\n';
		console.error(consoleMessage);
	}
}
