import { CLIENT_ROUTES, isPublicPath, SERVER_ROUTES } from '$lib/config/paths';
import { type Handle, redirect } from '@sveltejs/kit';
import { ApiService } from '$lib/utils/api.service';
import { CookieService } from '$lib/utils/cookie.service.ts';

export const handle: Handle = async ({ event, resolve }) => {
	let { accessToken, refreshToken } = CookieService.getAuthTokens(event.cookies);
	if (refreshToken || accessToken) {
		const apiResponse: ApiResponse = await ApiService.fetchFromServer(
			SERVER_ROUTES.USER_DETAILS.path,
			{
				method: 'GET',
				headers: {
					'Content-Type': 'application/json',
					Cookie: event.request.headers.get('cookie') || ''
				}
			},
			{
				cookies: event.cookies
			}
		);

		if (apiResponse.status === 401) {
			event.locals.user = null;
			CookieService.clearAuthCookies(event.cookies);
		} else {
			event.locals.user = apiResponse.data as UserDetailsResponse;
		}
	}

	if (isPublicPath(event.url.pathname)) {
		return resolve(event);
	}

	if (!accessToken) {
		throw redirect(303, CLIENT_ROUTES.LOGIN_PAGE.path);
	}
	return resolve(event);
};
