import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { CookieService } from '$lib/utils/cookie.service.ts';

export async function load({ params, cookies }) {
	const getDashboard = async () => {
		const id: string = params.id;
		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.DASHBOARD_CONFIGURATION.path.replace('{id}', id), {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			}
		});

		return response.data;
	};

	const getDashboards = async () => {
		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.DASHBOARDS.path, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			}
		});

		return response.data;
	};

	return {
		dashboard: await getDashboard(),
		dashboards: await getDashboards()
	};
}
