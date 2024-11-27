import { TOKEN_REFRESH_BUFFER_SECONDS } from '$lib/config/constants';
import { SERVER_ROUTES } from '$lib/config/paths';

export class JwtService {
	/**
	 * Checks if a token is expired
	 * @param token - The JWT token to check
	 * @param bufferSeconds - Optional buffer time in seconds before actual expiration
	 * Returns true if token is expired or invalid
	 */
	static isTokenExpired(token: string, bufferSeconds: number = TOKEN_REFRESH_BUFFER_SECONDS): boolean {
		const payload: JwtPayload | null = this.decodeToken(token);
		if (!payload) return true;

		const currentTime = Math.floor(Date.now() / 1000); // Convert to seconds
		return payload.exp <= currentTime + bufferSeconds;
	}

	/**
	 * Gets the time remaining until token expiration
	 * @returns Time remaining in seconds, or 0 if token is expired/invalid
	 */
	static getTimeUntilExpiry(token: string): number {
		const payload = this.decodeToken(token);
		if (!payload) return 0;

		const currentTime = Math.floor(Date.now() / 1000);
		const timeRemaining = payload.exp - currentTime;
		return Math.max(0, timeRemaining);
	}

	/**
	 * Gets the user details from the token.
	 * @returns User details, or null if token is invalid
	 * @param token - The JWT token to check
	 * @returns User details, or null if token is invalid
	 */
	static getUserDetailsFromToken(token: string): UserDetailsResponse {
		const payload: JwtPayload | null = this.decodeToken(token);
		if (!payload) throw new Error('Invalid token');
		return {
			id: 0,
			email: payload.sub,
			firstName: payload.first_name,
			lastName: payload.last_name,
			authority: payload.authority,
			permissions: payload.permissions,
			teams: payload.teams.map((team) => ({ name: team }) as TeamResponse),
			lastLogin: new Date(payload.last_login * 1000),
			created: new Date(payload.created * 1000),
			enabled: payload.enabled,
			validated: payload.validated
		};
	}

	static async refreshTokenPair(refreshToken: string): Promise<TokenPair | undefined> {
		try {
			const response: Response = await fetch(SERVER_ROUTES.REFRESH.path, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ refreshToken })
			});

			if (response.ok) {
				const refreshResponse: AuthorizeResponse = await response.json();
				return { accessToken: refreshResponse.accessToken, refreshToken: refreshResponse.refreshToken };
			}
		} catch (error) {
			console.error('Error refreshing token:', error);
		}

		return;
	}

	/**
	 * Validate token format without checking expiration
	 */
	private static isValidTokenFormat(token: string): boolean {
		try {
			return token.split('.').length === 3;
		} catch {
			return false;
		}
	}

	/**
	 * Decodes a JWT token and returns its payload
	 */
	private static decodeToken(token: string): JwtPayload | null {
		if (!this.isValidTokenFormat(token)) {
			return null;
		}

		try {
			const base64Url = token.split('.')[1];
			const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
			const payload = JSON.parse(
				decodeURIComponent(
					atob(base64)
						.split('')
						.map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
						.join('')
				)
			);
			return payload;
		} catch (error) {
			console.error('Invalid Token: Cannot decode token.', error);
			return null;
		}
	}
}
