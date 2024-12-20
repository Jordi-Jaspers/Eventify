import { User } from '$lib/models/user.svelte';
import { BreadcrumbNavigation } from '$lib/models/breadcrumb-navigation.svelte';
import { Teams } from '$lib/models/teams.svelte';
import { Users } from '$lib/models/users.svelte';
import { Options } from '$lib/models/options.svelte';
import { Dashboards } from '$lib/models/dashboards.svelte';
import { Localstorage } from '$lib/store/localstorage.svelte';

// Global State
export const user = new User();
export const users = new Users();
export const dashboards = new Dashboards();
export const teams = new Teams();
export const options = new Options();
export const breadcrumbs = new BreadcrumbNavigation();

// Local State
export const activeTeam = new Localstorage<TeamResponse>('activeTeam', { id: 0, name: 'No team assigned' } as TeamResponse);
