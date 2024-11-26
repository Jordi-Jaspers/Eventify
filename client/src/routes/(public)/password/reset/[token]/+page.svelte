<script lang="ts">
    import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '$lib/components/ui/card';
    import {Input} from '$lib/components/ui/input';
    import {toast} from 'svelte-sonner';
    import {Privacy, Submit} from "$lib/components/button";
    import type {SubmitFunction} from "@sveltejs/kit";
    import {applyAction, enhance} from "$app/forms";
    import {Logo, PasswordMeter} from "$lib/components/general";
    import {CardFooter} from "$lib/components/ui/card/index.js";
    import {Label} from "$lib/components/ui/label";

    let formData = $state({
        password: '',
        confirmation: ''
    });

    let isLoading = $state(false);
    const resetPassword: SubmitFunction = () => {
        toast.loading('Resetting password...');
        isLoading = true
        return async ({result}) => {
            toast.dismiss();
            isLoading = false
            if (result.type === 'success') {
                toast.success('Password reset successfully!');
                await applyAction(result)
            }

            if (result.type === 'failure' && result.data) {
                const response = result.data.response;
                toast.error(response.message);
            }
        }
    }

    let showPassword: boolean = $state(false);
    let isPasswordValid: boolean = $state(false);
    let isDisabled = $derived(isLoading || !isPasswordValid);
</script>

<div class="flex h-full flex-col">
    <Logo/>
    <div class="flex h-full w-full items-center justify-center px-4">
        <Card class="snake">
            <CardHeader>
                <CardTitle class="text-2xl">Reset Your Password</CardTitle>
                <CardDescription>Change your password to something new and make sure you remember it this time.
                </CardDescription>
            </CardHeader>
            <form id="reset" method="POST" use:enhance={resetPassword}>
                <CardContent class="grid gap-4">
                    <div class="grid gap-2">
                        <Label>Password</Label>
                        <div class="relative">
                            <Input
                                    id="password"
                                    name="password"
                                    type={showPassword ? 'text' : 'password'}
                                    bind:value={formData.password}
                                    placeholder="Password"
                                    autocomplete="current-password"
                                    required
                            />
                            <Privacy bind:enabled={showPassword}/>
                        </div>
                    </div>
                    <div class="grid gap-2">
                        <Label>Confirm Password</Label>
                        <div class="relative">
                            <Input
                                    id="confirmation"
                                    name="confirmation"
                                    type="password"
                                    autocomplete="new-password"
                                    bind:value={formData.confirmation}
                                    placeholder="Confirm password"
                                    required
                            />
                        </div>
                    </div>
                    <PasswordMeter password={formData.password}
                                   confirmation={formData.confirmation}
                                   bind:isValid={isPasswordValid}
                    />
                </CardContent>
            </form>
            <CardFooter>
                <Submit {isLoading} {isDisabled} title="Reset" form="reset"/>
            </CardFooter>
        </Card>
    </div>
</div>
