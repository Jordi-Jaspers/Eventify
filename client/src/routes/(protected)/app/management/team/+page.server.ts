import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { type Actions, fail } from '@sveltejs/kit';
import { CookieService } from '$lib/utils/cookie.service.ts';

export async function load({ cookies }) {
	const getTeams = async () => {
		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.TEAMS.path, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			}
		});

		return response.data;
	};

	const getUsers = async () => {
		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.USERS.path, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			}
		});

		return response.data;
	};

	return {
		teams: await getTeams(),
		users: await getUsers()
	};
}

export const actions: Actions = {
	createTeam: async ({ cookies, request }) => {
		const data: FormData = await request.formData();
		const input: TeamRequest = {
			name: data.get('name') as string,
			description: data.get('description') as string
		};

		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.TEAMS.path, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			},
			body: JSON.stringify(input)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	updateTeam: async ({ cookies, request }) => {
		const data: FormData = await request.formData();

		const id: string = data.get('id') as string;
		const input: TeamRequest = {
			name: data.get('name') as string,
			description: data.get('description') as string
		};

		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.TEAM.path.replace('{id}', id), {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			},
			body: JSON.stringify(input)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	deleteTeam: async ({ cookies, request }) => {
		const data: FormData = await request.formData();
		const id: string = data.get('id') as string;

		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.TEAM.path.replace('{id}', id), {
			method: 'DELETE',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			}
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	assignMember: async ({ request, cookies }) => {
		const data: FormData = await request.formData();
		const teamId: string = data.get('team_id') as string;
		const userId: string = data.get('user_id') as string;
		const teamMemberRequest: TeamMemberRequest = {
			userIds: [parseInt(userId)]
		};

		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.TEAM_MEMBERS.path.replace('{id}', teamId), {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			},
			body: JSON.stringify(teamMemberRequest)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	unassignMember: async ({ cookies, request }) => {
		const data: FormData = await request.formData();
		const teamId: string = data.get('team_id') as string;
		const userId: string = data.get('user_id') as string;
		const teamMemberRequest: TeamMemberRequest = {
			userIds: [parseInt(userId)]
		};

		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.TEAM_MEMBERS.path.replace('{id}', teamId), {
			method: 'DELETE',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			},
			body: JSON.stringify(teamMemberRequest)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	}
};
