import { Exception } from '$lib/models/exception.error';

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
					signal: AbortSignal.timeout(timeout)
				});

				if (!response.ok) {
					lastErrorResponse = response;
					throw new Error(`HTTP error! Status: ${response.status}`);
				}

				return { response: response, status: response.status };
			} catch (error) {
				attempts++;
				if (attempts >= retries) {
					if (lastErrorResponse) {
						const exception: Exception = new Exception(lastErrorResponse, await lastErrorResponse.json());
						return { error: exception.message, status: exception.status };
					}
					console.error(`Failed to fetch ${url} after ${retries} attempts`);
					return { error: 'Something went wrong processing your request.', status: 500 };
				}
			}
		}

		throw new Error(`Unexpected fetch failure for ${url}`);
	}
}
