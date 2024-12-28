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

interface DashboardResponse {
	id: number;
	name: string;
	description: string;
	global: boolean;
	created: Date;
	lastUpdated: Date;
	updatedBy: string;
	team: TeamResponse;
	configuration: DashboardConfigurationResponse;
}

interface DashboardConfigurationResponse {
	groups: DashboardGroupResponse[];
	ungroupedChecks: CheckResponse[];
}

interface DashboardGroupResponse {
	name: string;
	checks: CheckResponse[];
}

interface CheckResponse {
	id: number;
	name: string;
	created: Date;
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
