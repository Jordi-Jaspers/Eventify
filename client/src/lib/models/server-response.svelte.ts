export class ServerResponse {
	success: boolean = false;
	status: number = 500;
	message: string = 'An Unknown Error Occurred';
	duration: number = 0;
	method: string;
	path: string;
	headers: Map<string, string[]>;

	token?: string;
	data?: any;

	constructor(method: string, path: string, requestHeaders: HeadersInit | undefined) {
		this.method = method;
		this.path = path;
		this.headers = new Map<string, string[]>();

		if (requestHeaders) {
			const headers: Headers = new Headers(requestHeaders);
			if (headers.has('Authorization')) {
				const authHeader: string = headers.get('Authorization') as string;
				this.token = authHeader.split(' ')[1];
			}
		}
	}

	toApiResponse(): ApiResponse {
		const responseString: string = JSON.stringify(this);
		return JSON.parse(responseString) as ApiResponse;
	}
}
