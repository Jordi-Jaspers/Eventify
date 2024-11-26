import { Exception } from '$lib/models/exception.error';
import { CookieService } from '$lib/utils/cookie.service';

export class ApiService {
	/**
	 * Fetch with retry and timeout handling
	 * @param url The URL to fetch
	 * @param options Fetch options
	 * @param config Configuration for retries and timeout
	 * @returns {Promise<{response?: Response; error?: string}>}
	 */
	static async fetchWithRetry(url: string, options: RequestInit = {}, config: RetryConfig = {}): Promise<ApiResponse> {
		let attempts: number = 0;
		let lastErrorResponse: Response | null = null;

		const { retries = 3, timeout = 60_000 } = config;
		while (attempts < retries) {
			try {
				const response: Response = await fetch(url, {
					...options,
					credentials: 'include',
					signal: AbortSignal.timeout(timeout)
				});

				if (!response.ok) {
					lastErrorResponse = response;
					throw new Error(`HTTP error! Status: ${response.status}`);
				}

				const apiResponse: ApiResponse = {
					success: true,
					status: response.status,
					message: 'The request was successful with status code ' + response.status,
					data: response.status === 204 ? null : await response.json()
				};

				// TODO: log response
				console.log('API Response: ', apiResponse);
				return apiResponse;
			} catch (error) {
				attempts++;
				if (attempts >= retries) {
					if (lastErrorResponse) {
						const exception: Exception = new Exception(lastErrorResponse, await lastErrorResponse.json());
						const apiResponse: ApiResponse = {
							success: false,
							status: lastErrorResponse.status,
							message: exception.message,
							data: exception.throwable
						};

						// TODO: log exception
						console.log(apiResponse);
						return apiResponse;
					}

					console.error(`Failed to fetch ${url} after ${retries} attempts`);
					return {
						success: false,
						status: 500,
						message: 'Something went wrong processing your request.',
						data: null
					};
				}
			}
		}

		throw new Error(`Unexpected fetch failure for ${url}`);
	}
}
