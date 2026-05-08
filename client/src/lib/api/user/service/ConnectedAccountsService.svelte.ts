import { listProviders, unlinkProvider } from '../UserProviderController';
import type { OAuthProvider, ProviderResponse } from '$lib/api/models.ts';
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';
import { buildOAuth2Url } from '$lib/api/authentication/oauth2-url.ts';
import { requestPasswordReset } from '$lib/api/authentication/PasswordController.ts';

export function createConnectedAccountsService() {
    let providers: ProviderResponse[] = $state([]);
    let loading: boolean = $state(false);
    let unlinkingId: number | null = $state(null);
    let showUnlinkDialog: boolean = $state(false);
    let providerToUnlink: ProviderResponse | null = $state(null);
    let showChangePasswordDialog: boolean = $state(false);

    async function load(): Promise<void> {
        loading = true;
        try {
            providers = await listProviders();
        } catch (err: unknown) {
            const { message }: { message: string } = handleError(err, 'Failed to load providers');
            toast.error(message);
        } finally {
            loading = false;
        }
    }

    function linkProvider(provider: OAuthProvider): void {
        globalThis.location.href = buildOAuth2Url(provider, 'link');
    }

    function openUnlinkDialog(provider: ProviderResponse): void {
        providerToUnlink = provider;
        showUnlinkDialog = true;
    }

    function setShowUnlinkDialog(v: boolean): void {
        showUnlinkDialog = v;
        if (!v) {
            providerToUnlink = null;
        }
    }

    async function confirmUnlink(): Promise<void> {
        if (!providerToUnlink) return;

        const id: number = providerToUnlink.id!;
        setShowUnlinkDialog(false);
        unlinkingId = id;

        try {
            await unlinkProvider(id);
            toast.success('Provider unlinked');
            await load();
        } catch (err: unknown) {
            const { message }: { message: string } = handleError(err, 'Failed to unlink provider');
            toast.error(message);
            await load();
        } finally {
            unlinkingId = null;
        }
    }

    function openChangePasswordDialog(): void {
        showChangePasswordDialog = true;
    }

    function setShowChangePasswordDialog(v: boolean): void {
        showChangePasswordDialog = v;
    }

    async function requestPasswordResetForLocal(email: string): Promise<void> {
        try {
            await requestPasswordReset(email);
            toast.success('Password reset email sent');
        } catch (err: unknown) {
            const { message }: { message: string } = handleError(err, 'Failed to send reset email');
            toast.error(message);
        }
    }

    return {
        get providers(): ProviderResponse[] { return providers; },
        get loading(): boolean { return loading; },
        get unlinkingId(): number | null { return unlinkingId; },
        get showUnlinkDialog(): boolean { return showUnlinkDialog; },
        get providerToUnlink(): ProviderResponse | null { return providerToUnlink; },
        get showChangePasswordDialog(): boolean { return showChangePasswordDialog; },
        load,
        linkProvider,
        openUnlinkDialog,
        setShowUnlinkDialog,
        confirmUnlink,
        openChangePasswordDialog,
        setShowChangePasswordDialog,
        requestPasswordResetForLocal
    };
}

export type ConnectedAccountsService = ReturnType<typeof createConnectedAccountsService>;
