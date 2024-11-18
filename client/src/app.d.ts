declare global {
	declare module '*.png?enhanced';
	namespace App {
		interface Locals {
			user?: UserDetailsResponse;
		}
	}
}

// ======================== EXCEPTION ========================
class RetryConfig {
	retries?: number;
	timeout?: number;
}

class ApiResponse {
	response?: Response;
	error?: string;
}

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

class AuthorizeResponse extends UserDetailsResponse {
	accessToken: string;
	refreshToken: string;
	expiresAt: Date;
}

// ======================== JWT ========================

class TokenPair {
	accessToken: string;
	refreshToken: string;
}

class JwtPayload {
	sub: string;
	firstName: string;
	lastName: string;
	authorities: string[];
	lastLogin: Date;
	enabled: boolean;
	validated: boolean;
	exp: number;
	iat: number;
	iss: string;
}
