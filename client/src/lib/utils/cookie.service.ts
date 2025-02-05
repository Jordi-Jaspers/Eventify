import { ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE } from '$lib/config/constants';
import type { Cookies } from '@sveltejs/kit';
import { CLIENT_ROUTES } from '$lib/config/paths';
import type { CookieSerializeOptions } from 'cookie';

interface ParsedCookie {
	name: string;
	value: string;
	options: CookieSerializeOptions;
}

export class CookieService {
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

	/**
	 * Parse Set-Cookie headers into structured cookie objects
	 */
	static parseSetCookieHeaders(setCookieHeaders: string[]): ParsedCookie[] {
		if (!Array.isArray(setCookieHeaders)) {
			return [];
		}

		return setCookieHeaders.map((cookieStr) => {
			const [nameValuePair, ...parts] = cookieStr.split(';').map((p) => p.trim());
			const [name, value] = nameValuePair.split('=').map((p) => p.trim());

			const cookie: ParsedCookie = {
				name,
				value: decodeURIComponent(value),
				options: {}
			};

			parts.forEach((part) => {
				const [key, val] = part.split('=').map((p) => p.trim());
				const lowerKey = key.toLowerCase();

				switch (lowerKey) {
					case 'expires':
						cookie.options.expires = new Date(val);
						break;
					case 'max-age':
						cookie.options.maxAge = parseInt(val, 10);
						break;
					case 'domain':
						cookie.options.domain = val;
						break;
					case 'path':
						cookie.options.path = val;
						break;
					case 'samesite':
						cookie.options.sameSite = val.toLowerCase() as 'lax' | 'strict' | 'none';
						break;
					case 'secure':
						cookie.options.secure = true;
						break;
					case 'httponly':
						cookie.options.httpOnly = true;
						break;
					case 'partitioned':
						cookie.options.partitioned = true;
						break;
				}
			});

			return cookie;
		});
	}

	/**
	 * Set multiple cookies from Set-Cookie headers
	 */
	static setFromHeaders(cookies: Cookies, setCookieHeaders: string[]): void {
		const parsedCookies = this.parseSetCookieHeaders(setCookieHeaders);
		parsedCookies.forEach(({ name, value, options }) => {
			const finalOptions = {
				...options,
				path: options.path || CLIENT_ROUTES.LANDING_PAGE.path
			};
			cookies.set(name, value, finalOptions);
		});
	}

	/**
	 * Handle cookies from a Response object
	 */
	static async handleResponseCookies(cookies: Cookies, response: Response): Promise<ParsedCookie[]> {
		const setCookieHeaders = response.headers.getSetCookie();
		const parsedCookies = this.parseSetCookieHeaders(setCookieHeaders);
		this.setFromHeaders(cookies, setCookieHeaders);
		return parsedCookies;
	}
}
