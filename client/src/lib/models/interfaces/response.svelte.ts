interface PageableResponse {
	pageNumber: number;
	pageSize: number;
	sort: any;
	offset: number;
	paged: boolean;
	unpaged: boolean;
}

interface PageResponse<T> {
	content: T[];
	pageable: PageableResponse;
	totalElements: number;
	totalPages: number;
	last: boolean;
	numberOfElements: number;
	first: boolean;
	size: number;
	number: number;
}

interface RegisterResponse {
	email: string;
	authority: string;
	enabled: boolean;
	validated: boolean;
}

interface UserDetailsResponse {
	email: string;
	firstName: string;
	lastName: string;
	authority: string;
	permissions: string[];
	teams: TeamResponse[];
	lastLogin: Date;
	created: Date;
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
}

interface AuthorizeResponse extends UserDetailsResponse {
	accessToken: string;
	refreshToken: string;
	expiresAt: Date;
}
