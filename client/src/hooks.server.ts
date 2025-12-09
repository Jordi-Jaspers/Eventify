import {type Handle, redirect} from '@sveltejs/kit';
import {CLIENT_ROUTES, isPublicPath} from '$lib/config/routes';
import {CookieService} from "$lib/utils/cookie.service.ts";

export const handle: Handle = async ({event, resolve}) => {
    let {accessToken} = CookieService.getAuthTokens(event.cookies);

    // Check if user has auth cookies (accessToken)
    const {pathname}: { pathname: string } = event.url;
    const isAuthenticated: boolean = !!accessToken;

    // If user is authenticated and tries to access login page, redirect to dashboard
    if (isAuthenticated && pathname === CLIENT_ROUTES.LOGIN_PAGE.path) {
        throw redirect(302, CLIENT_ROUTES.DASHBOARD_PAGE.path);
    }

    // If user is not authenticated and tries to access protected route, redirect to login
    if (!isAuthenticated && !isPublicPath(pathname)) {
        throw redirect(302, CLIENT_ROUTES.LOGIN_PAGE.path);
    }

    // Continue with the request
    return resolve(event);
};
