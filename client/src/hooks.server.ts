import { CLIENT_ROUTES, isPublicPath, SERVER_ROUTES } from '$lib/config/paths';
import { CookieService } from '$lib/utils/cookie.service';
import { JwtService } from '$lib/utils/jwt.service';
import { type Handle, redirect } from '@sveltejs/kit';
import { ApiService } from '$lib/utils/api.service';

export const handle: Handle = async ({ event, resolve }) => {
	//----- event logging -----
	// TODO: capture logs.

	// ---- pre-processing ----
	// Try and retrieve tokens from cookies
	let { accessToken, refreshToken } = CookieService.getAuthTokens(event.cookies);

	// Update the access token if it is expired
	if (!accessToken || JwtService.isTokenExpired(accessToken)) {
		if (refreshToken && !JwtService.isTokenExpired(refreshToken)) {
			const tokens: TokenPair | undefined = await JwtService.refreshTokenPair(refreshToken);
			if (tokens) {
				accessToken = tokens.accessToken;
				refreshToken = tokens.refreshToken;
			}
		}
	}

	// if everything is good, try and set the user details in locals
	if (accessToken && refreshToken) {
		CookieService.setAuthCookies(event.cookies, { accessToken, refreshToken });
		const userDetails: UserDetailsResponse = JwtService.getUserDetailsFromToken(accessToken);
		const apiResponse: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.USER_DETAILS.path, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			}
		});

		userDetails.id = apiResponse.data.id;
		userDetails.teams = apiResponse.data.teams;
		event.locals.user = userDetails;
	}

	// ---- event handling ----
	// If the path is public, continue with the event.
	if (isPublicPath(event.url.pathname)) {
		return resolve(event);
	}

	// If the path is not public, check for access token.
	if (!accessToken) {
		event.locals.user = null;
		CookieService.clearAuthCookies(event.cookies);
		throw redirect(303, CLIENT_ROUTES.LOGIN_PAGE.path);
	} else {
		return resolve(event);
	}
};
