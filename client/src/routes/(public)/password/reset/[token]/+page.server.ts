import type { Actions, PageServerLoad } from './$types';
import { fail, redirect } from '@sveltejs/kit';
import { CLIENT_ROUTES, SERVER_ROUTES } from '$lib/config/paths';
import { CookieService } from '$lib/utils/cookie.service';
import { Exception } from '$lib/models/exception.error';
import { ApiService } from '$lib/utils/api.service';

export const actions: Actions = {
	default: async ({ params, request }) => {
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
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(input)
			},
			{
				retries: 1,
				timeout: 60_000
			}
		);

		if (response.success) {
			throw redirect(303, CLIENT_ROUTES.LOGIN_PAGE.path);
		}
		return fail(response.status, { response: response });
	}
};
