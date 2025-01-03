export class Teams {
	_teams: TeamResponse[] = $state([]);

	getTeams(): TeamResponse[] {
		return this._teams;
	}

	setTeams(teams: TeamResponse[]): void {
		this._teams = teams;
	}

	addTeam(team: TeamResponse): void {
		this._teams = [...this._teams, team];
	}

	removeTeam(id: number): void {
		this._teams = this._teams.filter((team) => team.id !== id);
	}

	clear(): void {
		this._teams = [];
	}

	updateTeam(id: number, updatedTeam: TeamResponse): void {
		this._teams = this._teams.map((team) => (team.id === id ? updatedTeam : team));
	}
}
