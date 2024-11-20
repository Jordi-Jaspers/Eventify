import type { PageServerLoad } from '../../.svelte-kit/types/src/routes/(public)/login/$types';

export const load: PageServerLoad = async ({ locals }) => {
	return {
		user: locals.user
	};
};
