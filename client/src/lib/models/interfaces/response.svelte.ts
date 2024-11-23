interface RegisterResponse {
	email: string;
	authorities: string[];
	enabled: boolean;
	validated: boolean;
}

interface UserDetailsResponse {
	email: string;
	firstName: string;
	lastName: string;
	authorities: string[];
	teams: TeamResponse[];
	lastLogin: Date;
	created: Date;
	enabled: boolean;
	validated: boolean;
}

interface TeamResponse {
	name: string;
	description: string;
	created: Date;
}

interface AuthorizeResponse extends UserDetailsResponse {
	accessToken: string;
	refreshToken: string;
	expiresAt: Date;
}
