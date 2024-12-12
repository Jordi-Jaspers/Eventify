import type { ServerResponse } from '$lib/models/server-response.svelte.js';
import chalk from 'chalk';
import { format } from 'date-fns';

enum LogLevel {
	INFO = 'INFO',
	ERROR = 'ERROR'
}

export class LogEntry {
	level: LogLevel = LogLevel.ERROR;
	status: number = 500;
	duration: number = 0;
	headers: Map<string, string>;
	method: string;
	path: string;

	user?: string;
	message?: string;
	data?: any;

	constructor(response: ServerResponse) {
		if (response.status < 400) this.level = LogLevel.INFO;
		this.status = response.status;
		this.method = response.method;
		this.path = response.path;
		this.headers = response.headers;
		this.duration = Math.round(response.duration);
		this.data = response.data;
	}

	log() {
		if (this.level === LogLevel.ERROR) {
			console.error(this.format());
		} else {
			console.log(this.format());
		}
	}

	private format(): string {
		const now = new Date();
		const formattedDate = format(now, 'yyyy-MM-dd HH:mm:ss.SSS');

		// Extract TX and REQ IDs from headers
		const txId = this.headers.get('x-hawaii-tx-id') || 'N/A';
		const reqId = this.headers.get('x-hawaii-request-id') || 'N/A';

		// Color the level based on log level
		const levelColor = this.level === LogLevel.ERROR ? chalk.red(this.level) : chalk.green(this.level);

		// Construct the log message
		const headerPart = `${formattedDate} ${levelColor} [${chalk.green(`TX:${txId}`)}/${chalk.yellow(`REQ:${reqId}`)}]`;
		const headersPart = Array.from(this.headers.entries())
			.map(([key, value]) => `\t${key}: ${value}`)
			.join('\n');

		// Construct the message part
		const messagePart =
			`\nServer Message: ${this.message || 'N/A'}\n` +
			`Duration: ${this.duration}ms\n` +
			(this.user ? `User: ${this.user}\n` : '') +
			`${this.method} ${this.path} - ${this.status}\n` +
			(headersPart ? `Headers:\n${headersPart}\n` : '') +
			(this.data ? `Body:\n${JSON.stringify(this.data)}\n` : '');

		return `${headerPart} -\n${messagePart}`;
	}
}
