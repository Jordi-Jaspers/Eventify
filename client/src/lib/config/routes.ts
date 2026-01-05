export const enum RouteType {
	PUBLIC = 'public',
	PRIVATE = 'private'
}

interface Route {
	path: string;
	type: RouteType;
}

/**
 * Client routes (frontend pages)
 */
export const CLIENT_ROUTES = {
	// Public routes
	LANDING_PAGE: {
		path: '/',
		type: RouteType.PUBLIC
	},
	LOGIN_PAGE: {
		path: '/login',
		type: RouteType.PUBLIC
	},
	REGISTER_PAGE: {
		path: '/register',
		type: RouteType.PUBLIC
	},
	VERIFY_EMAIL_PAGE: {
		path: '/verify',
		type: RouteType.PUBLIC
	},
	FORGOT_PASSWORD_PAGE: {
		path: '/forgot-password',
		type: RouteType.PUBLIC
	},
	RESET_PASSWORD_PAGE: {
		path: '/reset-password',
		type: RouteType.PUBLIC
	},
	OAUTH_REDIRECT_PAGE: {
		path: '/oauth2/redirect',
		type: RouteType.PUBLIC
	},

	// Private routes
	DASHBOARD_PAGE: {
		path: '/dashboard',
		type: RouteType.PRIVATE
	},
	PROFILE_PAGE: {
		path: '/profile',
		type: RouteType.PRIVATE
	},
	ADMIN_DASHBOARD_PAGE: {
		path: '/admin/dashboard',
		type: RouteType.PRIVATE
	},
	ADMIN_USERS_PAGE: {
		path: '/admin/users',
		type: RouteType.PRIVATE
	},
	ADMIN_ORGANIZATIONS_NEW: {
		path: '/admin/organizations/new',
		type: RouteType.PRIVATE
	},
	ADMIN_ORGANIZATIONS_PAGE: {
		path: '/admin/organizations',
		type: RouteType.PRIVATE
	},
	ORGANIZATION_MEMBERS_PAGE: (orgId: number) => ({
		path: `/organizations/${orgId}/members`,
		type: RouteType.PRIVATE
	}),
	ORGANIZATION_DASHBOARD_PAGE: (orgId: number) => ({
		path: `/organizations/${orgId}/dashboard`,
		type: RouteType.PRIVATE
	})
} as const;

/**
 * Server routes (backend API endpoints)
 */
export const SERVER_ROUTES = {
	ADMIN_STATS: '/admin/stats'
} as const;

/**
 * Get all public route paths
 */
export function getPublicRoutes(): string[] {
	return Object.values(CLIENT_ROUTES)
		.filter((route: Route | Function) => typeof route !== 'function' && route.type === RouteType.PUBLIC)
		.map((route: Route | Function) => (route as Route).path);
}

/**
 * Check if a given path is a public route
 */
export function isPublicPath(path: string): boolean {
	const cleanPath: string = path.endsWith('/') && path !== '/' ? path.slice(0, -1) : path;

	return (
		cleanPath === '' ||
		cleanPath === '/' ||
		getPublicRoutes().includes(cleanPath)
	);
}
