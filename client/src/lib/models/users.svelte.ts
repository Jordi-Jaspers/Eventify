export class Users {
	_users: UserDetailsResponse[] = $state([]);

	getUsers(): UserDetailsResponse[] {
		return this._users;
	}

	setUsers(users: UserDetailsResponse[]): void {
		this._users = users;
		this._users.forEach((user) => {
			user.name = `${user.firstName} ${user.lastName} ${user.email}`;
		});
	}

	updateUser(updatedUser: UserDetailsResponse): void {
		updatedUser.name = `${updatedUser.firstName} ${updatedUser.lastName} ${updatedUser.email}`;
		this._users = this._users.map((user: UserDetailsResponse): UserDetailsResponse => (user.id === updatedUser.id ? updatedUser : user));
	}

	clear(): void {
		this._users = [];
	}
}
