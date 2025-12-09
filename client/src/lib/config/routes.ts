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
	}
} as const;

/**
 * Get all public route paths
 */
export function getPublicRoutes(): string[] {
	return Object.values(CLIENT_ROUTES)
		.filter((route: Route) => route.type === RouteType.PUBLIC)
		.map((route: Route) => route.path);
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