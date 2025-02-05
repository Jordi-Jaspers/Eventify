import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';

export async function load({ params, locals, cookies }) {
	const verifyEmail: () => Promise<ApiResponse> = async (): Promise<ApiResponse> => {
		const url: string = new URL(SERVER_ROUTES.VERIFY_EMAIL.path) + '?token=' + params.token;
		const response: ApiResponse = await ApiService.fetchFromServer(
			url,
			{
				method: 'POST',
				headers: { 'Content-Type': 'application/json' }
			},
			{
				retries: 1,
				timeout: 60_000,
				cookies
			}
		);

		if (response.success) {
			locals.user = (await response.data) as AuthorizeResponse;
		}

		return response;
	};

	return {
		user: locals.user,
		response: await verifyEmail()
	};
}
