import { SERVER_BASE_URL } from '$lib/config/constants';

export const enum RouteType {
	PUBLIC = 'public',
	PRIVATE = 'private'
}

export const SERVER_ROUTES = {
	REGISTER: {
		path: `${SERVER_BASE_URL}/auth/register`,
		type: RouteType.PUBLIC
	},
	LOGIN: {
		path: `${SERVER_BASE_URL}/auth/login`,
		type: RouteType.PUBLIC
	},
	LOGOUT: {
		path: `${SERVER_BASE_URL}/auth/logout`,
		type: RouteType.PUBLIC
	},
	REFRESH: {
		path: `${SERVER_BASE_URL}/auth/token`,
		type: RouteType.PUBLIC
	},
	RESEND_EMAIL_VERIFICATION: {
		path: `${SERVER_BASE_URL}/auth/verify/resend`,
		type: RouteType.PUBLIC
	},
	VERIFY_EMAIL: {
		path: `${SERVER_BASE_URL}/auth/verify`,
		type: RouteType.PUBLIC
	},
	VALIDATE_EMAIL: {
		path: `${SERVER_BASE_URL}/public/email/validate`,
		type: RouteType.PUBLIC
	},
	FORGOT_PASSWORD: {
		path: `${SERVER_BASE_URL}/public/reset_password/request`,
		type: RouteType.PUBLIC
	},
	RESET_FORGOTTEN_PASSWORD: {
		path: `${SERVER_BASE_URL}/public/reset_password`,
		type: RouteType.PUBLIC
	},
	UPDATE_PASSWORD: {
		path: `${SERVER_BASE_URL}/password`,
		type: RouteType.PRIVATE
	},
	USERS: {
		path: `${SERVER_BASE_URL}/user`,
		type: RouteType.PRIVATE
	},
	USER: {
		path: `${SERVER_BASE_URL}/user/{id}`,
		type: RouteType.PRIVATE
	},
	LOCK_USER: {
		path: `${SERVER_BASE_URL}/user/{id}/lock`,
		type: RouteType.PRIVATE
	},
	UNLOCK_USER: {
		path: `${SERVER_BASE_URL}/user/{id}/unlock`,
		type: RouteType.PRIVATE
	},
	USER_DETAILS: {
		path: `${SERVER_BASE_URL}/user/details`,
		type: RouteType.PRIVATE
	},
	TEAMS: {
		path: `${SERVER_BASE_URL}/team`,
		type: RouteType.PRIVATE
	},
	TEAM: {
		path: `${SERVER_BASE_URL}/team/{id}`,
		type: RouteType.PRIVATE
	},
	TEAM_MEMBERS: {
		path: `${SERVER_BASE_URL}/team/{id}/members`,
		type: RouteType.PRIVATE
	},
	OPTIONS: {
		path: `${SERVER_BASE_URL}/options`,
		type: RouteType.PRIVATE
	},
	DASHBOARDS: {
		path: `${SERVER_BASE_URL}/dashboard`,
		type: RouteType.PRIVATE
	},
	DASHBOARD: {
		path: `${SERVER_BASE_URL}/dashboard/{id}`,
		type: RouteType.PRIVATE
	},
	DASHBOARD_CONFIGURATION: {
		path: `${SERVER_BASE_URL}/dashboard/{id}/configuration`,
		type: RouteType.PRIVATE
	}
};

export const CLIENT_ROUTES = {
	LANDING_PAGE: {
		path: '/',
		type: RouteType.PUBLIC
	},
	LOGIN_PAGE: {
		path: '/login',
		type: RouteType.PUBLIC
	},
	RESEND_EMAIL_VERIFICATION_PAGE: {
		path: '/verify/email/resend',
		type: RouteType.PUBLIC
	},
	FORGOT_PASSWORD_PAGE: {
		path: '/password/forgot',
		type: RouteType.PUBLIC
	},
	RESET_PASSWORD_PAGE: {
		path: '/password/reset',
		type: RouteType.PUBLIC
	},
	EMAIL_VERIFICATION_PAGE: {
		path: '/verify/email',
		type: RouteType.PUBLIC
	},
	LOGOUT_PAGE: {
		path: '/logout',
		type: RouteType.PRIVATE
	},
	APPLICATION_PAGE: {
		path: '/app',
		type: RouteType.PRIVATE
	},
	DASHBOARDS_PAGE: {
		path: '/app/dashboards',
		type: RouteType.PRIVATE
	},
	ACCOUNT_DETAILS_PAGE: {
		path: '/app/account',
		type: RouteType.PRIVATE
	},
	MONITORING_PAGE: {
		path: '/app/monitoring',
		type: RouteType.PRIVATE
	},
	TEAM_MANAGEMENT_PAGE: {
		path: '/app/management/teams',
		type: RouteType.PRIVATE
	},
	USER_MANAGEMENT_PAGE: {
		path: '/app/management/users',
		type: RouteType.PRIVATE
	}
};

export function getPublicRoutes(): string[] {
	return Object.values(CLIENT_ROUTES)
		.filter((route) => route.type === RouteType.PUBLIC)
		.map((route) => route.path);
}

export function isPublicPath(path: string): boolean {
	const cleanPath = path.endsWith('/') ? path.slice(0, -1) : path;
	return (
		cleanPath === '' ||
		getPublicRoutes().includes(cleanPath) ||
		cleanPath.startsWith(CLIENT_ROUTES.EMAIL_VERIFICATION_PAGE.path) ||
		cleanPath.startsWith(CLIENT_ROUTES.RESET_PASSWORD_PAGE.path)
	);
}
