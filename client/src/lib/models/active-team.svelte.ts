export class ActiveTeam {
	_activeTeam: TeamResponse = $state({} as TeamResponse);

	setActiveTeam(team: TeamResponse): void {
		this._activeTeam = team;
	}

	getActiveTeam(): TeamResponse {
		return this._activeTeam;
	}

	getId(): number {
		return this._activeTeam.id;
	}

	getName(): string {
		return this._activeTeam.name;
	}

	getDescription(): string {
		return this._activeTeam.description;
	}

	getCreated(): Date {
		return this._activeTeam.created;
	}

	getMembers(): TeamMemberResponse[] {
		return this._activeTeam.members;
	}
}
