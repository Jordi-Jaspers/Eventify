import { CLIENT_ROUTES, SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { CookieService } from '$lib/utils/cookie.service';
import { redirect } from '@sveltejs/kit';

export async function load({ cookies }) {
	const logout = async () => {
		await ApiService.fetchFromServer(SERVER_ROUTES.LOGOUT.path);
		CookieService.clearAuthCookies(cookies);
		throw redirect(303, CLIENT_ROUTES.LOGIN_PAGE.path);
	};

	return { response: await logout() };
}
