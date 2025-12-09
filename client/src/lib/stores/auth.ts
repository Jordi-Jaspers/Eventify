import type {Writable} from 'svelte/store';
import {derived, writable} from 'svelte/store';
import {
    login as apiLogin,
    logout as apiLogout,
    register as apiRegister
} from '$lib/api/authentication/AuthenticationController';
import {
    resendVerification as apiResendVerification,
    verifyEmail as apiVerifyEmail
} from '$lib/api/authentication/EmailVerificationController';
import {Localstorage} from '$lib/utils/localstorage.svelte';
import {handleError} from '$lib/utils/error-handler';
import {toast} from "svelte-sonner";
import type {RegisterRequest, RegisterResponse, UserDetailsResponse} from "$lib/api/models.ts";
import {getUserDetails as apiGetUserDetails} from "$lib/api/user/UserController.ts";

interface AuthState {
    user: UserDetailsResponse | null;
    loading: boolean;
    error: string | null;
}

// Create localStorage-backed reactive user state
const userStorage: Localstorage<UserDetailsResponse | null> = new Localstorage<UserDetailsResponse | null>(
    'user',
    null
);

// Create the auth store
function createAuthStore(): {
    subscribe: Writable<AuthState>['subscribe'];
    login: (email: string, password: string) => Promise<UserDetailsResponse>;
    register: (request: RegisterRequest) => Promise<RegisterResponse>;
    verifyEmail: (token: string) => Promise<UserDetailsResponse>;
    resendVerification: () => Promise<void>;
    logout: () => Promise<void>;
    setUser: (user: UserDetailsResponse | null) => void;
    initializeFromToken: () => void;
    clearError: () => void;
    clear: () => void;
} {
    const {subscribe, set, update}: Writable<AuthState> = writable<AuthState>({
        user: userStorage.value,
        loading: false,
        error: null
    });

    return {
        subscribe,
        login: async (email: string, password: string): Promise<UserDetailsResponse> => {
            update((state: AuthState): AuthState => ({...state, loading: true, error: null}));
            try {
                const user: UserDetailsResponse = await apiLogin({email, password});

                if (user) {
                    userStorage.value = user;
                    update((state: AuthState): AuthState => ({...state, user, loading: false}));

                    if (user.validated) {
                        toast.success('Welcome back!');
                    }
                }

                return user;
            } catch (error: unknown) {
                const {message}: { message: string } = handleError(error, 'Login failed');
                update((state: AuthState): AuthState => ({
                    ...state,
                    loading: false,
                    error: message
                }));
                throw error;
            }
        },
        register: async (request: RegisterRequest): Promise<RegisterResponse> => {
            update((state: AuthState): AuthState => ({...state, loading: true, error: null}));
            try {
                const response: RegisterResponse = await apiRegister(request);
                update((state: AuthState): AuthState => ({...state, loading: false}));
                toast.success('Registration successful! Please check your email to verify your account.');
                return response;
            } catch (error: unknown) {
                const {message}: { message: string } = handleError(error, 'Registration failed');
                update((state: AuthState): AuthState => ({
                    ...state,
                    loading: false,
                    error: message
                }));
                throw error;
            }
        },
        verifyEmail: async (token: string): Promise<UserDetailsResponse> => {
            update((state: AuthState): AuthState => ({...state, loading: true, error: null}));
            try {
                const user: UserDetailsResponse = await apiVerifyEmail(token);
                userStorage.value = user;

                update((state: AuthState): AuthState => ({...state, user, loading: false}));
                toast.success('Email verified successfully! Welcome to Eventify.');
                return user;
            } catch (error: unknown) {
                const {message}: { message: string } = handleError(error, 'Email verification failed');
                update((state: AuthState): AuthState => ({
                    ...state,
                    loading: false,
                    error: message
                }));
                throw error;
            }
        },
        resendVerification: async (): Promise<void> => {
            update((state: AuthState): AuthState => ({...state, loading: true, error: null}));
            try {
                if (userStorage.value?.email && !userStorage.value?.validated) {
                    await apiResendVerification(userStorage.value.email);
                    update((state: AuthState): AuthState => ({...state, loading: false}));
                    toast.success('Verification email sent. Please check your inbox.');
                }
            } catch (error: unknown) {
                const {message}: { message: string } = handleError(error, 'Resend verification failed');
                update((state: AuthState): AuthState => ({
                    ...state,
                    loading: false,
                    error: message
                }));
            }
        },
        logout: async (): Promise<void> => {
            update((state: AuthState): AuthState => ({...state, loading: true, error: null}));

            try {
                await apiLogout();
                userStorage.reset(null);

                set({user: null, loading: false, error: null});
                toast.success("You've been logged out.");
            } catch (error: unknown) {
                const {message}: { message: string } = handleError(error, 'Logout failed');
                update((state: AuthState): AuthState => ({
                    ...state,
                    loading: false,
                    error: message
                }));
            }
        },
        initializeFromToken: async (): Promise<void> => {
            try {
                const user: UserDetailsResponse = await apiGetUserDetails();
                userStorage.value = user;
                update((state: AuthState): AuthState => ({...state, user}));
            } catch (error: unknown) {
                // Backend returned error (no valid tokens or session expired)
                userStorage.reset(null);
                update((state: AuthState): AuthState => ({...state, user: null}));
            }
        },
        setUser: (user: UserDetailsResponse | null): void => {
            userStorage.value = user;
            update((state: AuthState): AuthState => ({...state, user}));
        },
        clearError: (): void => {
            update((state: AuthState): AuthState => ({...state, error: null}));
        },
        clear: (): void => {
            userStorage.reset(null);
            set({user: null, loading: false, error: null});
        }
    };
}

export const authStore = createAuthStore();
export const isAuthenticated = derived(
    authStore,
    ($auth: AuthState): boolean => $auth.user !== null && $auth.user.validated === true
);

export const isUnverified = derived(
    authStore,
    ($auth: AuthState): boolean => $auth.user !== null && $auth.user.validated === false
);

export const currentUser = derived(
    authStore,
    ($auth: AuthState): UserDetailsResponse | null => $auth.user
);