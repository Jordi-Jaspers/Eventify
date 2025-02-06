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

interface CheckResponse {
	id: number;
	name: string;
	created: Date;
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

enum Status {
	OK = 'bg-emerald-500',
	WARNING = 'bg-amber-500',
	CRITICAL = 'bg-red-500',
	DEGRADED = 'bg-orange-500',
	MAINTENANCE = 'bg-blue-500',
	DETACHED = 'bg-slate-500',
	UNKNOWN = 'bg-gray-300'
}

interface TimelineDurationResponse {
	startTime: Date;
	endTime: Date;
	status: Status;
}

interface TimelineResponse {
	durations: TimelineDurationResponse[];
}

interface CheckTimelineResponse {
	id: number;
	name: string;
	timeline: TimelineResponse;
}

interface GroupedTimelineResponse {
	id: number;
	name: string;
	timeline: TimelineResponse;
	checks: CheckTimelineResponse[];
}

interface DashboardSubscriptionResponse {
	dashboardId: number;
	window: number;
	timeline: TimelineResponse;
	groupedChecks: GroupedTimelineResponse[];
	ungroupedChecks: CheckTimelineResponse[];
}
