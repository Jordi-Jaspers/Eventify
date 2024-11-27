export class Users {
	_users: UserDetailsResponse[] = $state([]);

	getUsers(): UserDetailsResponse[] {
		return this._users;
	}

	setUsers(users: UserDetailsResponse[]): void {
		this._users = users;
	}

	clear(): void {
		this._users = [];
	}
}
