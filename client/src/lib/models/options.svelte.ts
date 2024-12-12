export class Options {
	_options: Map<string, any> = $state(new Map());

	setOptions(options: Map<string, any>): void {
		this._options = options;
	}

	getOptions(): Map<string, any> {
		return this._options;
	}

	getAuthorityOptions(): string[] {
		return this._options.authorities ?? [];
	}
}
