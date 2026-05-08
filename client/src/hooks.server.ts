import {type Handle, redirect} from '@sveltejs/kit';
import {CLIENT_ROUTES, isPublicPath} from '$lib/config/routes';
import {CookieService} from "$lib/api/authentication/service/cookie.service.ts";

export const handle: Handle = async ({event, resolve}) => {
    const {accessToken, refreshToken} = CookieService.getAuthTokens(event.cookies);
    const {pathname}: { pathname: string } = event.url;

    // User has a valid session if either token exists
    // Backend will handle token refresh if access token expired but refresh token valid
    const hasValidSession: boolean = !!accessToken || !!refreshToken;

    // If user has session and tries to access login page, redirect to dashboard
    if (hasValidSession && pathname === CLIENT_ROUTES.LOGIN_PAGE.path) {
        throw redirect(302, CLIENT_ROUTES.DASHBOARD_PAGE.path);
    }

    // If user has no session and tries to access protected route, redirect to login
    if (!hasValidSession && !isPublicPath(pathname)) {
        throw redirect(302, CLIENT_ROUTES.LOGIN_PAGE.path);
    }

    // Continue with the request
    return resolve(event);
};
