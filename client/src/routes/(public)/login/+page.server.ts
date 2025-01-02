import type { Actions, PageServerLoad } from './$types';
import { fail, redirect } from '@sveltejs/kit';
import { CLIENT_ROUTES, SERVER_ROUTES } from '$lib/config/paths';
import { CookieService } from '$lib/utils/cookie.service';
import { ApiService } from '$lib/utils/api.service';

export const load: PageServerLoad = async ({ locals }) => {
	if (locals.user && locals.user.validated && locals.user.enabled) {
		throw redirect(303, CLIENT_ROUTES.APPLICATION_PAGE.path);
	}
};

export const actions: Actions = {
	login: async ({ cookies, request, locals }) => {
		const data = await request.formData();
		const input: LoginRequest = {
			email: data.get('email') as string,
			password: data.get('password') as string
		};

		const response: ApiResponse = await ApiService.fetchFromServer(SERVER_ROUTES.LOGIN.path, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(input)
		});

		if (response.success) {
			const authorizeResponse: AuthorizeResponse = await response.data;
			const tokenPair: TokenPair = {
				accessToken: authorizeResponse.accessToken,
				refreshToken: authorizeResponse.refreshToken
			};

			CookieService.setAuthCookies(cookies, tokenPair);
			locals.user = authorizeResponse;

			if (authorizeResponse?.validated && authorizeResponse?.enabled) {
				throw redirect(303, CLIENT_ROUTES.APPLICATION_PAGE.path);
			}
		}
		return response.success ? { response: response } : fail(response.status, { response: response });
	},
	register: async ({ request }) => {
		const data = await request.formData();
		const input: RegisterRequest = {
			firstName: data.get('firstName') as string,
			lastName: data.get('lastName') as string,
			email: data.get('email') as string,
			password: data.get('password') as string,
			passwordConfirmation: data.get('passwordConfirmation') as string
		};

		const response: ApiResponse = await ApiService.fetchFromServer(
			SERVER_ROUTES.REGISTER.path,
			{
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(input)
			},
			{
				retries: 1,
				timeout: 60_000
			}
		);

		return response.success ? { response: response } : fail(response.status, { response: response });
	}
};
