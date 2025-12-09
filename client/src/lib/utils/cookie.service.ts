import { ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE } from '$lib/config/constants';
import type { Cookies } from '@sveltejs/kit';
import {CLIENT_ROUTES} from "$lib/config/routes.ts";

interface TokenPair {
    accessToken: string;
    refreshToken: string;
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
     * Checks if auth cookies are present in the document
     */
    static hasAuthCookies(): boolean {
        if (typeof document === "undefined") return false;
        return document.cookie.split("; ").some(c => c.startsWith(ACCESS_TOKEN_COOKIE + "="))
            && document.cookie.split("; ").some(c => c.startsWith(REFRESH_TOKEN_COOKIE + "="));
    }
}
