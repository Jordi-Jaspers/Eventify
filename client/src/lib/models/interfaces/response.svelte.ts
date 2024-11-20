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
	lastLogin: Date;
	created: Date;
	enabled: boolean;
	validated: boolean;
}

interface AuthorizeResponse extends UserDetailsResponse {
	accessToken: string;
	refreshToken: string;
	expiresAt: Date;
}
