import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';

export async function load({ params, request }) {
	const getDashboard = async () => {
		const id: string = params.id;
		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.DASHBOARD_CONFIGURATION.path.replace('{id}', id), {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Cookie: request.headers.get('cookie') || ''
			}
		});

		return response.data;
	};

	return {
		dashboard: await getDashboard()
	};
}
