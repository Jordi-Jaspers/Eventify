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
	}
};

export const CLIENT_ROUTES = {
	HOMEPAGE: {
		path: '/',
		type: RouteType.PUBLIC
	},
	LOGIN_PAGE: {
		path: '/login',
		type: RouteType.PUBLIC
	},
	RESEND_EMAIL_VERIFICATION_PAGE: {
		path: '/verify/resend',
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
	ACCOUNT_DETAILS_PAGE: {
		account: '/account',
		type: RouteType.PRIVATE
	},
	DASHBOARD_PAGE: {
		path: '/dashboard',
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
