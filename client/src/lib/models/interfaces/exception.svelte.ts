interface DefaultException {
	method: string;
	uri: string;
	query: null;
	contentType: string;
	statusCode: number;
	statusMessage: string;
	errorMessage: string;
}

interface ApiException extends DefaultException {
	apiErrorCode: string;
	apiErrorReason: string;
}

interface ValidationException extends DefaultException {
	errors: ValidationField[];
}

interface ValidationField {
	code: string;
	field: string;
}
