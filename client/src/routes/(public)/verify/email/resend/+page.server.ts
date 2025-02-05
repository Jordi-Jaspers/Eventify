import { SERVER_ROUTES } from '$lib/config/paths';
import { ApiService } from '$lib/utils/api.service';
import type { Actions } from '@sveltejs/kit';

export const actions: Actions = {
	default: async ({ request }) => {
		const data = await request.formData();
		const url: string = (new URL(SERVER_ROUTES.RESEND_EMAIL_VERIFICATION.path) + '?email=' + data.get('email')) as string;
		await ApiService.fetchFromServer(url, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' }
		});
	}
};
