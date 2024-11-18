import { CLIENT_ROUTES, isPublicPath, SERVER_ROUTES } from '$lib/config/paths';
import { CookieService } from '$lib/utils/cookie.service';
import { JwtService } from '$lib/utils/jwt.service';
import { type Handle, redirect } from '@sveltejs/kit';

export const handle: Handle = async ({ event, resolve }) => {
	// Skip auth check for public routes
	if (isPublicPath(event.url.pathname)) {
		return resolve(event);
	}

	// If no tokens exist, redirect to login.
	const { accessToken, refreshToken } = CookieService.getAuthTokens(event.cookies);
	if (!accessToken && !refreshToken) {
		throw redirect(303, CLIENT_ROUTES.LOGIN_PAGE.path);
	}

	// If Access Token is valid, store user details in locals and continue
	if (accessToken && !JwtService.isTokenExpired(accessToken)) {
		const userDetails: UserDetailsResponse = JwtService.getUserDetailsFromToken(accessToken);
		event.locals.user = JwtService.getUserDetailsFromToken(accessToken);
		if (!userDetails?.validated || !userDetails?.enabled) {
			throw redirect(303, CLIENT_ROUTES.LOGIN_PAGE.path);
		}
		return resolve(event);
	}

	// If we reach here, access token is either expired or will expire soon
	if (refreshToken && !JwtService.isTokenExpired(refreshToken)) {
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
				const tokens: TokenPair = { accessToken: refreshResponse.accessToken, refreshToken };
				if (tokens.accessToken && !JwtService.isTokenExpired(tokens.accessToken)) {
					CookieService.setAuthCookies(event.cookies, tokens);
					event.locals.user = refreshResponse;
					return await resolve(event);
				}
			}
		} catch (error) {
			event.locals.user = null;
			CookieService.clearAuthCookies(event.cookies);
		}
	}

	// If we reach here, refresh token is expired or invalid
	event.locals.user = null;
	CookieService.clearAuthCookies(event.cookies);
	throw redirect(303, CLIENT_ROUTES.LOGIN_PAGE.path);
};
