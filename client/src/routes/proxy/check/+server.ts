import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { type RequestHandler } from '@sveltejs/kit';

export const GET: RequestHandler = async ({ url, request }) => {
	const queryParams: string = url.search;
	const requestUrl: string = SERVER_ROUTES.CHECK_SEARCH.path + queryParams;
	const response: ApiResponse = await ApiService.fetchFromServer(requestUrl, {
		method: 'GET',
		headers: {
			Cookie: request.headers.get('cookie') || ''
		}
	});

	return new Response(JSON.stringify(response.data), { status: response.status });
};
