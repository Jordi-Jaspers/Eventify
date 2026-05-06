import type { Component } from 'svelte';
import { GitBranch, Globe, KeyRound } from '@lucide/svelte';
import type { OAuthProvider, ProviderType } from '$lib/api/models';

/**
 * Display metadata for an authentication provider.
 * Single source of truth for icons + labels used across login buttons,
 * connected accounts, and any other provider-aware UI.
 */
export interface ProviderMeta {
    label: string;
    icon: Component;
}

const PROVIDER_META: Record<ProviderType, ProviderMeta> = {
    LOCAL: { label: 'Local password', icon: KeyRound },
    GOOGLE: { label: 'Google', icon: Globe },
    GITHUB: { label: 'GitHub', icon: GitBranch }
};

export function getProviderMeta(provider: ProviderType): ProviderMeta {
    return PROVIDER_META[provider];
}

/**
 * Convert a backend provider enum to the lowercase OAuth provider key
 * used in OAuth2 redirect URLs (e.g. 'GOOGLE' -> 'google').
 *
 * Throws for non-OAuth providers (e.g. 'LOCAL') so callers cannot accidentally
 * build an invalid OAuth URL.
 */
export function toOAuthProvider(provider: ProviderType): OAuthProvider {
    if (provider !== 'GOOGLE' && provider !== 'GITHUB') {
        throw new Error(`Provider ${provider} is not an OAuth provider`);
    }
    return provider.toLowerCase() as OAuthProvider;
}
