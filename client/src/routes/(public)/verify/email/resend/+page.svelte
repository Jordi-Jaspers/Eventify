<script lang="ts">
    import {Activity} from 'lucide-svelte';
    import {Input} from "$lib/components/ui/input";
    import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "$lib/components/ui/card";
    import {applyAction, enhance} from '$app/forms';
    import type {SubmitFunction} from "@sveltejs/kit";
    import {Submit} from "$lib/components/button";
    import {toast} from "svelte-sonner";
    import {CLIENT_ROUTES} from "$lib/config/paths";

    let formData = $state({
        email: ''
    });

    let isLoading = $state(false);
    const resend: SubmitFunction = () => {
        isLoading = true
        return async ({result}) => {
            isLoading = false
            toast.success('If the email exists, a verification email will be sent to it.');
            await applyAction(result)
        }
    }
</script>

<div class="flex h-full flex-col">
    <a href={CLIENT_ROUTES.LOGIN_PAGE.path} class="flex p-4">
        <Activity class="mr-2 inline-block h-6 w-6"/>
        <h4>Eventify.io</h4>
    </a>

    <div class="flex h-full w-full items-center justify-center px-4">
        <Card class="snake">
            <CardHeader>
                <CardTitle class="text-2xl">Resend Verification</CardTitle>
                <CardDescription>Where should we send the verification message to?</CardDescription>
            </CardHeader>
            <CardContent class="grid w-full items-center gap-4">
                <form id="resend" method="POST" use:enhance={resend}>
                    <div class="w-full max-w-sm items-center space-x-2 grid grid-cols-5">
                        <Input
                                class="col-span-4"
                                id="email"
                                name="email"
                                type="email"
                                bind:value={formData.email}
                                placeholder="Enter your email..."
                                autocomplete="username"
                                required
                        />
                        <Submit {isLoading} isDisabled={isLoading} title="Resend" form="resend"/>
                    </div>
                </form>
            </CardContent>
        </Card>
    </div>
</div>
