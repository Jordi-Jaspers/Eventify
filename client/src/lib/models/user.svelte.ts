export class User {
	_user: UserDetailsResponse | null = $state(null);

	get email(): string | null {
		return this._user?.email ?? null;
	}

	get firstName(): string | null {
		return this._user?.firstName ?? null;
	}

	get lastName(): string | null {
		return this._user?.lastName ?? null;
	}

	get fullName(): string | null {
		return this._user ? `${this._user.firstName} ${this._user.lastName}`.trim() : null;
	}

	get initials(): string | null {
		if (!this._user) return null;
		return this._user.firstName[0] + this._user.lastName[0];
	}

	get authority(): string | null {
		return this._user?.authority ?? null;
	}

	get permissions(): string[] {
		return this._user?.permissions ?? [];
	}

	get teams(): TeamResponse[] {
		return this._user?.teams ?? [];
	}

	get lastLogin(): Date | null {
		return this._user?.lastLogin ?? null;
	}

	get created(): Date | null {
		return this._user?.created ?? null;
	}

	get enabled(): boolean {
		return this._user?.enabled ?? false;
	}

	get validated(): boolean {
		return this._user?.validated ?? false;
	}

	setDetails(details: UserDetailsResponse) {
		this._user = details;
	}

	clear(): void {
		this._user = null;
	}
}
