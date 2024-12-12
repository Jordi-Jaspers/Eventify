import { User } from '$lib/models/user.svelte';
import { BreadcrumbNavigation } from '$lib/models/breadcrumb-navigation.svelte';
import { Teams } from '$lib/models/teams.svelte';
import { Users } from '$lib/models/users.svelte';
import { Options } from '$lib/models/options.svelte';

export const user = new User();
export const users = new Users();
export const teams = new Teams();
export const options = new Options();
export const breadcrumbs = new BreadcrumbNavigation();
