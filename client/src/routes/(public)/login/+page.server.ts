import type { Actions, PageServerLoad } from './$types';
import { fail, redirect } from '@sveltejs/kit';
import { CLIENT_ROUTES, SERVER_ROUTES } from '$lib/config/paths';
import { CookieService } from '$lib/utils/cookie.service';
import { Exception } from '$lib/models/exception.error';

export const load: PageServerLoad = async ({ locals }) => {
	if (locals.user && locals.user.validated && locals.user.enabled) {
		throw redirect(303, CLIENT_ROUTES.DASHBOARD_PAGE.path);
	}
};

export const actions: Actions = {
	login: async ({ cookies, request, locals }) => {
		const data = await request.formData();
		const input: LoginRequest = {
			email: data.get('email') as string,
			password: data.get('password') as string
		};

		const response: Response = await fetch(SERVER_ROUTES.LOGIN.path, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(input)
		});

		if (response.ok) {
			const authorizeResponse: AuthorizeResponse = await response.json();
			const tokenPair: TokenPair = {
				accessToken: authorizeResponse.accessToken,
				refreshToken: authorizeResponse.refreshToken
			};

			CookieService.setAuthCookies(cookies, tokenPair);
			locals.user = authorizeResponse;

			if (authorizeResponse?.validated && authorizeResponse?.enabled) {
				throw redirect(303, CLIENT_ROUTES.DASHBOARD_PAGE.path);
			}
			throw redirect(303, CLIENT_ROUTES.LOGIN_PAGE.path);
		}

		const exception: Exception = new Exception(response, await response.json());
		return fail(exception.status, { error: exception.message });
	},
	register: async ({ request }) => {
		const data = await request.formData();
		console.log(data);
		const input: RegisterRequest = {
			firstName: data.get('firstName') as string,
			lastName: data.get('lastName') as string,
			email: data.get('email') as string,
			password: data.get('password') as string,
			passwordConfirmation: data.get('passwordConfirmation') as string
		};

		const response: Response = await fetch(SERVER_ROUTES.REGISTER.path, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(input)
		});

		if (response.ok) {
			return { success: true };
		}

		const exception: Exception = new Exception(response, await response.json());
		return fail(exception.status, { error: exception.message });
	}
};
