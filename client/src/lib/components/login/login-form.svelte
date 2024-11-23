<script lang="ts">
    import {CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from '$lib/components/ui/card';
    import {Input} from '$lib/components/ui/input';
    import {Label} from '$lib/components/ui/label';
    import {Privacy, Submit} from '$lib/components/button/index.js';
    import GithubButton from '$lib/components/login/github.svelte';
    import OTPButton from '$lib/components/login/otp.svelte';
    import {CLIENT_ROUTES} from "$lib/config/paths";
    import type {SubmitFunction} from "@sveltejs/kit";
    import {applyAction, enhance} from '$app/forms';
    import {toast} from "svelte-sonner";
    import {user} from "$lib/store/global";

    let showPassword: boolean = $state(false);
    let formData = $state<LoginRequest>({
        email: '',
        password: ''
    });

    let isLoading = $state(false);
    const login: SubmitFunction = () => {
        toast.loading('Logging in...');
        isLoading = true
        return async ({result}) => {
            toast.dismiss();
            isLoading = false
            if (result.type === 'redirect') {
                await applyAction(result)
            }

            if (result.type === 'success') {
                if (!result.data.user.validated) {
                    toast.warning('Please verify your email address to continue, or request a new verification email.');
                    return;
                }

                if (!result.data.user.enabled) {
                    toast.warning('Your account has been disabled, please contact support.');
                    return;
                }
                await applyAction(result)
            }

            if (result.type === 'failure') {
                toast.error(result.data.error);
            }
        }
    }
</script>

<form id="login" action="?/login" method="POST" use:enhance={login}>
    <CardHeader>
        <CardTitle class="text-2xl">Sign In</CardTitle>
        <CardDescription>Enter your email and password to access your account and start your monitoring journey
        </CardDescription>
    </CardHeader>
    <CardContent class="grid w-full items-center gap-4">
        <div class="grid gap-2">
            <div class="flex flex-row items-center justify-between">
                <Label>Email</Label>
                {#if user._user && !user.validated}
                    <a href={CLIENT_ROUTES.RESEND_EMAIL_VERIFICATION_PAGE.path}
                       class="text-sm text-blue-500 hover:underline">Resend Validation</a>
                {/if}
            </div>
            <Input
                    name="email"
                    type="email"
                    bind:value={formData.email}
                    placeholder="johndoe@example.com"
                    autocomplete="email"
                    required
            />
        </div>
        <div class="grid gap-2">
            <div class="flex flex-row items-center justify-between">
                <Label>Password</Label>
                <a href={CLIENT_ROUTES.FORGOT_PASSWORD_PAGE.path} class="text-sm text-blue-500 hover:underline">Forgot
                    password?</a>
            </div>
            <div class="relative">
                <Input
                        name="password"
                        type={showPassword ? 'text' : 'password'}
                        bind:value={formData.password}
                        placeholder="Password"
                        autocomplete="current-password"
                        class="pr-10"
                        required
                />
                <Privacy bind:enabled={showPassword}/>
            </div>
        </div>

        <Submit {isLoading} isDisabled={isLoading} title="Log in" form="login"/>
        <div class="relative">
            <div class="absolute inset-0 flex items-center">
                <span class="w-full border-t"> </span>
            </div>
            <div class="relative flex justify-center text-xs uppercase">
                <span class="bg-background px-2 text-muted-foreground"> Or continue with </span>
            </div>
        </div>
    </CardContent>
    <CardFooter class="flex flex-col space-y-2">
        <GithubButton {isLoading}/>
        <OTPButton {isLoading}/>
    </CardFooter>
</form>
