import { listSessions, revokeSession, revokeAllOtherSessions } from '../UserSessionController';
import type { SessionResponse } from '$lib/api/models';
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';

export function createSessionService() {
    let sessions: SessionResponse[] = $state([]);
    let loading: boolean = $state(false);
    let revokingId: number | null = $state(null);

    // Dialog state for revoke single
    let showRevokeDialog: boolean = $state(false);
    let sessionToRevoke: SessionResponse | null = $state(null);

    // Dialog state for revoke all others
    let showRevokeAllDialog: boolean = $state(false);
    let revokingAll: boolean = $state(false);

    async function load(): Promise<void> {
        loading = true;
        try {
            sessions = await listSessions();
        } catch (err: unknown) {
            const { message }: { message: string } = handleError(err, 'Failed to load sessions');
            toast.error(message);
        } finally {
            loading = false;
        }
    }

    function openRevokeDialog(session: SessionResponse): void {
        sessionToRevoke = session;
        showRevokeDialog = true;
    }

    function openRevokeAllDialog(): void {
        showRevokeAllDialog = true;
    }

    async function confirmRevokeSession(): Promise<void> {
        if (!sessionToRevoke) return;

        const id: number = sessionToRevoke.id;
        revokingId = id;
        showRevokeDialog = false;

        try {
            await revokeSession(id);
            toast.success('Session revoked');
            await load();
        } catch (err: unknown) {
            const { message }: { message: string } = handleError(err, 'Failed to revoke session');
            toast.error(message);
        } finally {
            revokingId = null;
            sessionToRevoke = null;
        }
    }

    async function confirmRevokeAllOthers(): Promise<void> {
        revokingAll = true;
        showRevokeAllDialog = false;

        try {
            await revokeAllOtherSessions();
            toast.success('All other sessions revoked');
            await load();
        } catch (err: unknown) {
            const { message }: { message: string } = handleError(err, 'Failed to revoke sessions');
            toast.error(message);
        } finally {
            revokingAll = false;
        }
    }

    return {
        get sessions(): SessionResponse[] { return sessions; },
        get loading(): boolean { return loading; },
        get revokingId(): number | null { return revokingId; },
        get showRevokeDialog(): boolean { return showRevokeDialog; },
        get sessionToRevoke(): SessionResponse | null { return sessionToRevoke; },
        get showRevokeAllDialog(): boolean { return showRevokeAllDialog; },
        get revokingAll(): boolean { return revokingAll; },
        load,
        confirmRevokeSession,
        confirmRevokeAllOthers,
        openRevokeDialog,
        openRevokeAllDialog,
        setShowRevokeDialog: (v: boolean): void => { showRevokeDialog = v; },
        setShowRevokeAllDialog: (v: boolean): void => { showRevokeAllDialog = v; }
    };
}

export type SessionService = ReturnType<typeof createSessionService>;
