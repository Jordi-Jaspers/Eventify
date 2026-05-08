import createClient from 'openapi-fetch';
import type { paths } from '$lib/types/api';
import { SERVER_BASE_URL } from '$lib/config/constants';
import { goto } from '$app/navigation';
import { CLIENT_ROUTES, isPublicPath } from '$lib/config/routes';
import { toast } from 'svelte-sonner';
import { browser } from '$app/environment';

/* Flag to prevent multiple simultaneous redirects on 401 */
let isRedirecting: boolean = false;

/* Create an API client using openapi-fetch */
export const client = createClient<paths>({
	baseUrl: SERVER_BASE_URL,
	credentials: 'include',
	headers: {
		'Content-Type': 'application/json',
	}
});

/* Add middleware to handle 401 errors globally */
client.use({
	async onResponse({ response, request }): Promise<Response | undefined> {
		if (response.status === 401 && browser) {
			const url: URL = new URL(request.url);
			const pathname: string = url.pathname;
			
			const isAuthEndpoint: boolean = pathname.includes('/auth/') || pathname.includes('/login');
			const currentPath: string = window.location.pathname;
			const isCurrentlyOnLoginPage: boolean = currentPath === CLIENT_ROUTES.LOGIN_PAGE.path;
			const isOnPublicPage: boolean = isPublicPath(currentPath);
			
			// Only redirect if we're not on a public page, not on the login page,
			// and the request wasn't to an auth endpoint
			if (!isRedirecting && !isCurrentlyOnLoginPage && !isAuthEndpoint && !isOnPublicPage) {
				isRedirecting = true;
				
				try {
					localStorage.removeItem('user');
				} catch (err: unknown) {
					console.error('Failed to clear localStorage:', err);
				}
				
				toast.error('Your session has expired. Please log in again.');
				await goto(`${CLIENT_ROUTES.LOGIN_PAGE.path}?expired=true`);
				setTimeout((): void => {
					isRedirecting = false;
				}, 1000);
			}
		}
		
		return response;
	}
});
