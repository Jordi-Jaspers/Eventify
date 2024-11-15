declare global {
	// declare const __NAME__: string;
	// declare const __VERSION__: string;
	namespace App {}
	declare module '*.png?enhanced';
}

// ======================== EXCEPTION ========================
class Exception {
	method: string;
	uri: string;
	query: null;
	contentType: string;
	statusCode: number;
	statusMessage: string;
	errorMessage: string;
	apiErrorCode: string;
	apiErrorReason: string;
}

class ValidationException {
	method: string;
	uri: string;
	query: null;
	contentType: string;
	statusCode: number;
	statusMessage: string;
	errorMessage: string;
	errors: ValidationField[];
}

class ValidationField {
	code: string;
	field: string;
}

// ======================== REQUESTS ========================
class PasswordRequest {
	newPassword: string;
	confirmPassword: string;
}

class UpdatePasswordRequest extends PasswordRequest {
	oldPassword: string;
}

class ForgotPasswordRequest extends PasswordRequest {
	token: string;
}

class LoginRequest {
	email: string;
	password: string;
}

class PageRequest {
	page: number;
	perPage: number;
}

class RefreshTokenRequest {
	refreshToken: string;
}

class RegisterRequest {
	firstName: string;
	lastName: string;
	email: string;
	password: string;
	passwordConfirmation: string;
}

class UpdateUserDetailsRequest {
	firstName: string;
	lastName: string;
}

class UpdateEmailRequest {
	email: string;
}

// ======================== RESPONSE ========================

class PageResponse<T> {
	content: T[];
	totalElements: number;
	totalPages: number;
	pageNumber: number;
	pageSize: number;
}

class RegisterResponse {
	email: string;
	authorities: string[];
	enabled: boolean;
	validated: boolean;
}

class UserDetailsResponse {
	email: string;
	firstName: string;
	lastName: string;
	authorities: string[];
	lastLogin: Date;
	enabled: boolean;
	validated: boolean;
}

class AuthorizeResponse {
	email: string;
	authorities: string[];
	lastLogin: Date;
	accessToken: string;
	refreshToken: string;
	expiresAt: Date;
	enabled: boolean;
	validated: boolean;
}

// ======================== JWT ========================

class JwtPayload {
	authorities: string[];
	exp: number;
	iat: number;
	iss: string;
	sub: string;
}
