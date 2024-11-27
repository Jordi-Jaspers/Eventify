import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { CookieService } from '$lib/utils/cookie.service';
import { type Actions, fail } from '@sveltejs/kit';

export async function load({ cookies }) {
	const getTeams = async () => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.TEAMS.path, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			}
		});

		return response.data;
	};

	const getUsers = async () => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.USERS.path, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			}
		});

		return response.data;
	};

	return {
		teams: await getTeams(),
		members: await getUsers()
	};
}

export const actions: Actions = {
	createTeam: async ({ request, cookies }) => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const data: FormData = await request.formData();
		const input: TeamRequest = {
			name: data.get('name') as string,
			description: data.get('description') as string
		};

		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.TEAMS.path, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			},
			body: JSON.stringify(input)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	updateTeam: async ({ request, cookies }) => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const data: FormData = await request.formData();

		const id: string = data.get('id') as string;
		const input: TeamRequest = {
			name: data.get('name') as string,
			description: data.get('description') as string
		};

		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.TEAM.path.replace('{id}', id), {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			},
			body: JSON.stringify(input)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	deleteTeam: async ({ request, cookies }) => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const data: FormData = await request.formData();
		const id: string = data.get('id') as string;

		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.TEAM.path.replace('{id}', id), {
			method: 'DELETE',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			}
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	assignMember: async ({ request, cookies }) => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const data: FormData = await request.formData();
		const teamId: string = data.get('team_id') as string;
		const userId: string = data.get('user_id') as string;
		const teamMemberRequest: TeamMemberRequest = {
			userIds: [parseInt(userId)]
		};

		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.TEAM_MEMBERS.path.replace('{id}', teamId), {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			},
			body: JSON.stringify(teamMemberRequest)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	unassignMember: async ({ request, cookies }) => {
		const { accessToken } = CookieService.getAuthTokens(cookies);
		const data: FormData = await request.formData();
		const teamId: string = data.get('team_id') as string;
		const userId: string = data.get('user_id') as string;
		const teamMemberRequest: TeamMemberRequest = {
			userIds: [parseInt(userId)]
		};

		const response: ApiResponse = await ApiService.fetchWithRetry(SERVER_ROUTES.TEAM_MEMBERS.path.replace('{id}', teamId), {
			method: 'DELETE',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			},
			body: JSON.stringify(teamMemberRequest)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	}
};
