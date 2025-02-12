import { CLIENT_ROUTES, SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { type Actions, fail, redirect } from '@sveltejs/kit';
import { CookieService } from '$lib/utils/cookie.service.ts';

export async function load({ cookies, params }) {
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

	return {
		dashboard: await getDashboard()
	};
}

export const actions: Actions = {
	configure: async ({ cookies, params, request }) => {
		const dashboardId: string = params.id as string;
		const data: FormData = await request.formData();

		const url: string = SERVER_ROUTES.DASHBOARD_CONFIGURATION.path.replace('{id}', dashboardId);
		const configuration: DashboardConfigurationResponse = JSON.parse(data.get('configuration') as string);
		const configurationRequest: DashboardConfigurationRequest = mapConfigurationToRequest(configuration);
		const response: ApiResponse = await ApiService.fetchFromServer(url, {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			},
			body: JSON.stringify(configurationRequest)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	configureAndMonitor: async ({ request, cookies, params }) => {
		const dashboardId: string = params.id as string;
		const data: FormData = await request.formData();

		const url: string = SERVER_ROUTES.DASHBOARD_CONFIGURATION.path.replace('{id}', dashboardId);
		const configuration: DashboardConfigurationResponse = JSON.parse(data.get('configuration') as string);
		const configurationRequest: DashboardConfigurationRequest = mapConfigurationToRequest(configuration);
		const response: ApiResponse = await ApiService.fetchFromServer(url, {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			},
			body: JSON.stringify(configurationRequest)
		});

		const monitoringPage: string = CLIENT_ROUTES.DASHBOARD_MONITORING_PAGE.path.replace('{id}', params.id as string);
		return response.success ? redirect(303, monitoringPage) : fail(response.status, { response: response });
	}
};

function mapConfigurationToRequest(configuration: DashboardConfigurationResponse): DashboardConfigurationRequest {
	const ungroupedCheckIds: number[] = configuration.ungroupedChecks.map((check) => check.id);
	const groups: DashboardGroupRequest[] = configuration.groups.map((group) => {
		const checkIds: number[] = group.checks.map((check) => check.id);
		return { name: group.name, checkIds: checkIds };
	});

	return { groups: groups, ungroupedCheckIds: ungroupedCheckIds };
}
