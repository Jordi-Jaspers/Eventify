import { ACCESS_TOKEN_COOKIE } from '$lib/config/constants';
import type { UserDetailsResponse } from '$lib/api/models';

/**
 * Interface representing the decoded JWT payload structure
 */
interface JwtPayload {
	// Standard JWT claims
	sub: string; // Email (subject)
	iss: string; // Issuer
	aud: string[]; // Audience
	exp: number; // Expiration time (epoch seconds)
	iat: number; // Issued at (epoch seconds)
	jti: string; // JWT ID

	// Custom claims
	email: string;
	first_name: string;
	last_name: string;
	role: 'USER' | 'ADMIN';
	permissions: string[];
	enabled: boolean;
	validated: boolean;
	last_login: number; // Epoch seconds
	created: number; // Epoch seconds
}

/**
 * JWT Service for client-side JWT token operations
 *
 * This service provides utilities for decoding and validating JWT tokens
 * stored in browser cookies. It does NOT verify token signatures (that's
 * the backend's responsibility), but it does decode and extract claims.
 */
export class JwtService {
	/**
	 * Decodes a JWT token and returns the payload
	 *
	 * @param token - The JWT token string
	 * @returns The decoded payload
	 * @throws Error if the token is malformed or invalid
	 */
	static decodeToken(token: string): JwtPayload {
		try {
			// JWT structure: header.payload.signature
			const parts: string[] = token.split('.');

			if (parts.length !== 3) {
				throw new Error('Invalid JWT format: expected 3 parts separated by dots');
			}

			// Decode the payload (second part)
			const payload: string = parts[1];

			// Base64 URL decode (replace URL-safe characters and pad if needed)
			const base64: string = payload.replace(/-/g, '+').replace(/_/g, '/');
			const paddedBase64: string = base64.padEnd(
				base64.length + ((4 - (base64.length % 4)) % 4),
				'='
			);

			// Decode from base64 and parse JSON
			const decodedString: string = atob(paddedBase64);
			const decodedPayload: JwtPayload = JSON.parse(decodedString);

			return decodedPayload;
		} catch (error: unknown) {
			const message: string = error instanceof Error ? error.message : 'Unknown error';
			throw new Error(`Failed to decode JWT token: ${message}`);
		}
	}

	/**
	 * Extracts user details from a JWT token's claims
	 *
	 * Maps JWT claims to the UserDetailsResponse structure used throughout the app
	 *
	 * @param token - The JWT token string
	 * @returns UserDetailsResponse object with user information
	 * @throws Error if the token cannot be decoded or is missing required claims
	 */
	static extractUserDetails(token: string): UserDetailsResponse {
		const payload: JwtPayload = this.decodeToken(token);

		// Validate required claims exist
		if (!payload.email || !payload.first_name || !payload.last_name) {
			throw new Error('JWT token is missing required user claims');
		}

		// Convert epoch seconds to ISO 8601 date strings (format expected by UserDetailsResponse)
		const lastLogin: string = new Date(payload.last_login * 1000).toISOString();
		const created: string = new Date(payload.created * 1000).toISOString();

		return {
			email: payload.email,
			firstName: payload.first_name,
			lastName: payload.last_name,
			role: payload.role,
			permissions: payload.permissions as ('ACCESS_APPLICATION' | 'MANAGE_USERS')[],
			enabled: payload.enabled,
			validated: payload.validated,
			lastLogin: lastLogin,
			created: created
		};
	}

	/**
	 * Checks if a JWT token is expired
	 *
	 * @param token - The JWT token string
	 * @returns true if the token is expired, false otherwise
	 */
	static isTokenExpired(token: string): boolean {
		try {
			const payload: JwtPayload = this.decodeToken(token);
			const now: number = Math.floor(Date.now() / 1000);
			return payload.exp <= now;
		} catch (error: unknown) {
			return true;
		}
	}
}
