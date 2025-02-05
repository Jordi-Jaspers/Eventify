declare module '*.png?enhanced';
declare namespace App {
	interface Locals {
		user: UserDetailsResponse | null;
	}
}

interface RetryConfig {
	retries?: number;
	timeout?: number;
}

interface ApiResponse {
	success: boolean;
	status: number;
	headers: Map<string, string>;
	message: string;
	data: any;
}

interface TokenPair {
	accessToken: string;
	refreshToken: string;
}

interface JwtPayload {
	sub: string;
	first_name: string;
	last_name: string;
	authority: string;
	permissions: string[];
	teams: string[];
	last_login: number;
	created: number;
	enabled: boolean;
	validated: boolean;
	exp: number;
	iat: number;
	iss: string;
}
