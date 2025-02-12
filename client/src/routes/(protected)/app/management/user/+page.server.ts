import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { type Actions, fail } from '@sveltejs/kit';
import { CookieService } from '$lib/utils/cookie.service.ts';

export async function load({ cookies }) {
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

	const getOptions = async () => {
		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.OPTIONS.path, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			}
		});

		return response.data;
	};

	return {
		users: await getUsers(),
		options: await getOptions()
	};
}

export const actions: Actions = {
	lockUser: async ({ cookies, request }) => {
		const data: FormData = await request.formData();
		const path: string = SERVER_ROUTES.LOCK_USER.path.replace('{id}', data.get('id') as string);
		const response: ApiResponse = await ApiService.fetchFromServer(path, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			}
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	unlockUser: async ({ cookies, request }) => {
		const data: FormData = await request.formData();
		const path: string = SERVER_ROUTES.UNLOCK_USER.path.replace('{id}', data.get('id') as string);
		const response: ApiResponse = await ApiService.fetchFromServer(path, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			}
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	updateAuthority: async ({ cookies, request }) => {
		const data: FormData = await request.formData();
		const id: string = data.get('id') as string;
		const newAuthority: string = data.get('authority') as string;
		const input: UpdateAuthorityRequest = {
			authority: newAuthority.toUpperCase()
		};

		const path: string = SERVER_ROUTES.USER.path.replace('{id}', id);
		const response: ApiResponse = await ApiService.fetchFromServer(path, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				Cookie: CookieService.getCookies(cookies)
			},
			body: JSON.stringify(input)
		});

		return response.success ? { response: response } : fail(response.status, { response: response });
	}
};
