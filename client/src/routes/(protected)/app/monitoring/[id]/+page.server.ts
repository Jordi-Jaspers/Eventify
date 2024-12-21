import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { CookieService } from '$lib/utils/cookie.service';

export async function load({ cookies, params }) {
	const getDashboard = async () => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const id: string = params.id;
		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.DASHBOARD_CONFIGURATION.path.replace('{id}', id), {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			}
		});

		return response.data;
	};

	return {
		dashboard: await getDashboard()
	};
}
