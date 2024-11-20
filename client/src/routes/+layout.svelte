<script lang="ts">
    import '../app.css';

    import {Toaster} from '$lib/components/ui/sonner';
    import {ModeWatcher} from 'mode-watcher';
    import {user} from "$lib/store/global";

    let {data, children} = $props();

    $effect(() => {
        if (!data.user) user.clear();
        user.setDetails(data.user);
    });
</script>

<main class="h-screen">
    <ModeWatcher defaultTheme={'dark'}/>
    <Toaster/>
    {@render children()}
</main>
