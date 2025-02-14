import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { type RequestHandler } from '@sveltejs/kit';
import { CookieService } from '$lib/utils/cookie.service.ts';

export const GET: RequestHandler = async ({ url, cookies }) => {
	const queryParams: string = url.search;
	const requestUrl: string = SERVER_ROUTES.CHECK_SEARCH.path + queryParams;
	const response: ApiResponse = await ApiService.fetchFromServer(requestUrl, {
		method: 'GET',
		headers: {
			Cookie: CookieService.getCookies(cookies)
		}
	});

	return new Response(JSON.stringify(response.data), { status: response.status });
};
