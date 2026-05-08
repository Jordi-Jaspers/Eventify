import type { components } from '$lib/types/api.d.ts';

type ProviderResponse = components['schemas']['ProviderResponse'];

export function aConnectedGoogleProvider(overrides: Partial<ProviderResponse> = {}): ProviderResponse {
    return {
        id: 1,
        provider: 'GOOGLE',
        connected: true,
        providerEmail: 'user@gmail.com',
        ...overrides
    };
}

export function aConnectedGithubProvider(overrides: Partial<ProviderResponse> = {}): ProviderResponse {
    return {
        id: 2,
        provider: 'GITHUB',
        connected: true,
        providerEmail: 'user@github.com',
        ...overrides
    };
}

export function aDisconnectedGoogleProvider(): ProviderResponse {
    return {
        id: undefined,
        provider: 'GOOGLE',
        connected: false,
        providerEmail: undefined
    };
}

export function aDisconnectedGithubProvider(): ProviderResponse {
    return {
        id: undefined,
        provider: 'GITHUB',
        connected: false,
        providerEmail: undefined
    };
}

export function aLocalProvider(overrides: Partial<ProviderResponse> = {}): ProviderResponse {
    return {
        id: 10,
        provider: 'LOCAL',
        connected: true,
        providerEmail: undefined,
        ...overrides
    };
}

export function aProviderList(): ProviderResponse[] {
    return [aConnectedGoogleProvider(), aDisconnectedGithubProvider()];
}

export function aProviderListWithLocal(): ProviderResponse[] {
    return [aLocalProvider(), aConnectedGoogleProvider(), aDisconnectedGithubProvider()];
}
