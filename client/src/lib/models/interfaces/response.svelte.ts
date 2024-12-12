interface PageableResponse {
	number: number;
	size: number;
	totalElements: number;
	totalPages: number;
}

interface PageResponse<T> {
	content: T[];
	page: PageableResponse;
}

interface RegisterResponse {
	email: string;
	authority: string;
	enabled: boolean;
	validated: boolean;
}

interface TeamResponse {
	id: number;
	name: string;
	description: string;
	created: Date;
	members: TeamMemberResponse[];
}

interface TeamMemberResponse {
	id: number;
	email: string;
	firstName: string;
	lastName: string;
	name?: string;
}

interface UserDetailsResponse extends TeamMemberResponse {
	authority: string;
	permissions: string[];
	teams: TeamResponse[];
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
