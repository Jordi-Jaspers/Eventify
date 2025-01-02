import { LoggerService } from '$lib/utils/logger.service';
import { ServerResponse } from '$lib/models/server-response.svelte.js';
import { Exception } from '$lib/models/exception.error';

export class ApiService {
	/**
	 * Fetch with retry and timeout handling
	 * @param url The URL to fetch
	 * @param options Fetch options
	 * @param config Configuration for retries and timeout
	 * @returns {Promise<{response?: Response; error?: string}>}
	 */
	static async fetchFromServer(url: string, options: RequestInit = {}, config: RetryConfig = {}): Promise<ApiResponse> {
		const { retries = 3, timeout = 60_000 } = config;
		let lastErrorResponse: Response | null = null;
		const apiResponse = new ServerResponse(options.method || 'GET', url, options.headers);

		for (let attempts: number = 0; attempts < retries; attempts++) {
			const start: number = performance.now();
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

				return this.handleSuccessResponse(response, apiResponse, start);
			} catch (error) {
				if (attempts === retries - 1) {
					return this.handleErrorResponse(lastErrorResponse, apiResponse, start, retries);
				}
			}
		}
		throw new Error(`Unexpected fetch failure for ${url}`);
	}

	private static async handleSuccessResponse(response: Response, apiResponse: ServerResponse, start: number): Promise<ApiResponse> {
		apiResponse.success = true;
		apiResponse.status = response.status;
		apiResponse.headers = this.extractHeaders(response);
		apiResponse.duration = performance.now() - start;
		apiResponse.message = `Request successful with status ${response.status}`;
		apiResponse.data = response.status === 204 ? null : await response.json();
		LoggerService.log(apiResponse);
		return apiResponse.toApiResponse();
	}

	private static async handleErrorResponse(
		lastErrorResponse: Response | null,
		apiResponse: ServerResponse,
		start: number,
		retries: number
	): Promise<ApiResponse> {
		if (lastErrorResponse) {
			try {
				const exception = new Exception(lastErrorResponse, await lastErrorResponse.json());
				apiResponse.status = lastErrorResponse.status;
				apiResponse.headers = this.extractHeaders(lastErrorResponse);
				apiResponse.duration = performance.now() - start;
				apiResponse.message = exception.message;
				apiResponse.data = exception.throwable;
			} catch {
				// Fallback if JSON parsing fails
				apiResponse.status = 500;
				apiResponse.message = `Failed to parse error response from ${apiResponse.method} ${apiResponse.path}`;
			}
		} else {
			// Generic network error
			apiResponse.status = 500;
			apiResponse.message = `Failed to fetch ${apiResponse.method} ${apiResponse.path} after ${retries} attempts`;
		}

		apiResponse.duration = performance.now() - start;
		LoggerService.log(apiResponse);
		return apiResponse.toApiResponse();
	}

	private static extractHeaders(response: Response): Map<string, string> {
		const headersMap = new Map<string, string>();
		response.headers.forEach((value, key) => headersMap.set(key, value));
		return headersMap;
	}
}
