declare global {
	declare module '*.png?enhanced';
	namespace App {
		interface Locals {
			user?: UserDetailsResponse;
		}
	}
}

interface RetryConfig {
	retries?: number;
	timeout?: number;
}

interface ApiResponse {
	response?: Response;
	error?: string;
	status: number;
}

interface TokenPair {
	accessToken: string;
	refreshToken: string;
}

interface JwtPayload {
	sub: string;
	first_name: string;
	last_name: string;
	authorities: string[];
	last_login: number;
	created: number;
	enabled: boolean;
	validated: boolean;
	exp: number;
	iat: number;
	iss: string;
}
