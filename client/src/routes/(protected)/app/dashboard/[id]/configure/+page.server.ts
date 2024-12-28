import { CLIENT_ROUTES, SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { CookieService } from '$lib/utils/cookie.service';
import { type Actions, fail, redirect } from '@sveltejs/kit';

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

export const actions: Actions = {
	configure: async ({ request, cookies, params }) => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const dashboardId: string = params.id as string;
		const data: FormData = await request.formData();

		const url: string = SERVER_ROUTES.DASHBOARD_CONFIGURATION.path.replace('{id}', dashboardId);
		const configurationRequest: DashboardConfigurationRequest = JSON.parse(data.get('configuration') as string);

		const response: ApiResponse = await ApiService.fetchWithRetry(url, {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			},
			body: JSON.stringify(configurationRequest)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	configureAndMonitor: async ({ request, cookies, params }) => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const dashboardId: string = params.id as string;
		const data: FormData = await request.formData();

		const url: string = SERVER_ROUTES.DASHBOARD_CONFIGURATION.path.replace('{id}', dashboardId);
		const dashboardConfiguration: DashboardConfigurationRequest = JSON.parse(data.get('form') as string);
		const response: ApiResponse = await ApiService.fetchWithRetry(url, {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			},
			body: JSON.stringify(dashboardConfiguration)
		});

		const monitoringPage: string = CLIENT_ROUTES.DASHBOARD_MONITORING_PAGE.path.replace('{id}', params.id as string);
		return response.success ? redirect(303, monitoringPage) : fail(response.status, { response: response });
	}
};
