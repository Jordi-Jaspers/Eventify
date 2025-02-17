import type { Actions } from './$types';
import { fail, redirect } from '@sveltejs/kit';
import { CLIENT_ROUTES, SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { CookieService } from '$lib/utils/cookie.service.ts';

export const actions: Actions = {
	default: async ({ params, request, cookies }) => {
		const data: FormData = await request.formData();
		const input: ForgotPasswordRequest = {
			newPassword: data.get('password') as string,
			confirmPassword: data.get('confirmation') as string,
			token: params.token
		};

		const response: ApiResponse = await ApiService.fetchFromServer(
			SERVER_ROUTES.RESET_FORGOTTEN_PASSWORD.path,
			{
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Cookie: CookieService.getCookies(cookies)
				},
				body: JSON.stringify(input)
			},
			{
				retries: 1,
				timeout: 60_000,
				cookies
			}
		);

		if (response.success) {
			throw redirect(303, CLIENT_ROUTES.LOGIN_PAGE.path);
		}
		return fail(response.status, { response: response });
	}
};
