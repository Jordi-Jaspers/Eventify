import type { ServerResponse } from '$lib/models/server-response.svelte.js';
import { LogEntry } from '$lib/models/log-entry.svelte';
import { JwtService } from '$lib/utils/jwt.service';

export class LoggerService {
	static log(response: ServerResponse) {
		const entry: LogEntry = new LogEntry(response);
		if (response.token) {
			const user: UserDetailsResponse = JwtService.getUserDetailsFromToken(response.token);
			entry.user = user.firstName + ' ' + user.lastName + ' <' + user.email + '>';
		}

		entry.message = response.message;
		entry.log();
	}
}
