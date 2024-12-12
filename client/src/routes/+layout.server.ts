export const load = async ({ locals, url }) => {
	let paths: string[] = url.pathname
		.split('/')
		.filter((path) => path !== '')
		.filter((path) => path !== 'app')
		.map((path) => path.charAt(0).toUpperCase() + path.slice(1));

	return {
		user: locals.user,
		paths: paths
	};
};
