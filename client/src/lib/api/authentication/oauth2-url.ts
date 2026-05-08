import { SERVER_BASE_URL } from '$lib/config/constants.ts';
import type { OAuthProvider } from '$lib/api/models.ts';

/**
 * Backend OAuth2 authorization endpoint base path.
 * Single source of truth for the OAuth2 redirect URL.
 */
export const OAUTH2_AUTHORIZATION_PATH: string = '/v1/oauth2/authorization';

/**
 * Build the full OAuth2 authorization URL for a provider.
 *
 * @param provider - OAuth provider (google or github)
 * @param mode - Optional flow mode. Use 'link' to link to an existing
 *               authenticated account instead of logging in.
 */
export function buildOAuth2Url(provider: OAuthProvider, mode?: 'link'): string {
    const url: string = `${SERVER_BASE_URL}${OAUTH2_AUTHORIZATION_PATH}/${provider}`;
    return mode ? `${url}?mode=${mode}` : url;
}
