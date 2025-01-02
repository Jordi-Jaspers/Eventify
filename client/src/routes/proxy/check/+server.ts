import { CookieService } from '$lib/utils/cookie.service';
import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { type RequestHandler } from '@sveltejs/kit';

export const GET: RequestHandler = async ({ url, cookies }) => {
	const { accessToken } = CookieService.getAuthTokens(cookies);
	const queryParams: string = url.search;

	const requestUrl: string = SERVER_ROUTES.CHECK_SEARCH.path + queryParams;
	const response: ApiResponse = await ApiService.fetchFromServer(requestUrl, {
		method: 'GET',
		headers: {
			Authorization: `Bearer ${accessToken}`
		}
	});

	return new Response(JSON.stringify(response.data), { status: response.status });
};
