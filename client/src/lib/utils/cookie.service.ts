import { ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE } from '$lib/config/constants';
import type { Cookies } from '@sveltejs/kit';
import { JwtService } from '$lib/utils/jwt.service';
import { CLIENT_ROUTES } from '$lib/config/paths';

export class CookieService {
	/**
	 * Sets authentication cookies with proper security settings
	 */
	static setAuthCookies(cookies: Cookies, tokens: TokenPair): void {
		const accessExpiry = JwtService.getTimeUntilExpiry(tokens.accessToken);
		const refreshExpiry = JwtService.getTimeUntilExpiry(tokens.refreshToken);
		cookies.set(ACCESS_TOKEN_COOKIE, tokens.accessToken, {
			httpOnly: true,
			secure: true,
			sameSite: 'strict',
			path: CLIENT_ROUTES.LANDING_PAGE.path,
			maxAge: accessExpiry
		});

		cookies.set(REFRESH_TOKEN_COOKIE, tokens.refreshToken, {
			httpOnly: true,
			secure: true,
			sameSite: 'strict',
			path: CLIENT_ROUTES.LANDING_PAGE.path,
			maxAge: refreshExpiry
		});
	}

	/**
	 * Updates just the access token cookie
	 */
	static updateAccessToken(cookies: Cookies, accessToken: string): void {
		const expiry = JwtService.getTimeUntilExpiry(accessToken);
		cookies.set(ACCESS_TOKEN_COOKIE, accessToken, {
			httpOnly: true,
			secure: true,
			sameSite: 'strict',
			path: CLIENT_ROUTES.LANDING_PAGE.path,
			maxAge: expiry
		});
	}

	/**
	 * Retrieves both tokens from cookies
	 */
	static getAuthTokens(cookies: Cookies): Partial<TokenPair> {
		return {
			accessToken: cookies.get(ACCESS_TOKEN_COOKIE),
			refreshToken: cookies.get(REFRESH_TOKEN_COOKIE)
		};
	}

	/**
	 * Clears authentication cookies
	 */
	static clearAuthCookies(cookies: Cookies): void {
		cookies.delete(ACCESS_TOKEN_COOKIE, { path: CLIENT_ROUTES.LANDING_PAGE.path });
		cookies.delete(REFRESH_TOKEN_COOKIE, { path: CLIENT_ROUTES.LANDING_PAGE.path });
	}
}
