import type { PageServerLoad } from '../../.svelte-kit/types/src/routes/(public)/login/$types';

export const load: PageServerLoad = async ({ locals, url }) => {
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
