import createClient from 'openapi-fetch';
import type { paths } from '$lib/types/api';
import { SERVER_BASE_URL } from '$lib/config/constants';

/* Create an API client using openapi-fetch */
export const client = createClient<paths>({
	baseUrl: SERVER_BASE_URL,
	credentials: 'include',
	headers: {
		'Content-Type': 'application/json',
	}
});
