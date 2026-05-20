import { SERVER_BASE_URL } from '$lib/config/constants';
import type { ChangelogEntry } from '$lib/api/models';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ fetch }): Promise<{ changelog: ChangelogEntry[] }> => {
	const response: Response = await fetch(`${SERVER_BASE_URL}/v1/public/changelog`);

	if (!response.ok) {
		return { changelog: [] };
	}

	const changelog: ChangelogEntry[] = await response.json();
	return { changelog };
};
