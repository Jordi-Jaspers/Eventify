import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import { sleep } from '$lib/utils';

export async function load({ params }) {
	const verifyEmail = async (): Promise<ApiResponse> => {
		const url: string = new URL(SERVER_ROUTES.VERIFY_EMAIL.path) + '?token=' + params.token;
		sleep(5000);
		return await ApiService.fetchWithRetry(url, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' }
		});
	};

	return { response: await verifyEmail() };
}
