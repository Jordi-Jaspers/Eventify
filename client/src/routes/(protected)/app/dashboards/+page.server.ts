import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { CookieService } from '$lib/utils/cookie.service';
import { type Actions, fail } from '@sveltejs/kit';

export async function load({ cookies }) {
	const getDashboards = async () => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.DASHBOARDS.path, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			}
		});

		return response.data;
	};

	return {
		dashboards: await getDashboards()
	};
}

export const actions: Actions = {
	createDashboard: async ({ request, cookies }) => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const data: FormData = await request.formData();
		const input: DashboardCreationRequest = {
			name: data.get('name') as string,
			description: data.get('description') as string,
			teamId: data.get('teamId') ? parseInt(data.get('teamId') as string) : 0,
			global: data.get('global') ? data.get('global') === 'on' : false
		};

		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.DASHBOARDS.path, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			},
			body: JSON.stringify(input)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	deleteDashboard: async ({ request, cookies }) => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const data: FormData = await request.formData();
		const id: string = data.get('id') as string;

		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.DASHBOARD.path.replace('{id}', id), {
			method: 'DELETE',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			}
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	updateDashboard: async ({ request, cookies }) => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const data: FormData = await request.formData();
		const id: string = data.get('id') as string;
		const input: DashboardUpdateRequest = {
			name: data.get('name') as string,
			description: data.get('description') as string,
			teamId: data.get('teamId') ? parseInt(data.get('teamId') as string) : 0,
			global: data.get('global') ? data.get('global') === 'on' : false
		};

		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.DASHBOARD.path.replace('{id}', id), {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			},
			body: JSON.stringify(input)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	}
};
