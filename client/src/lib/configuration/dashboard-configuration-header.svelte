<script lang="ts">
    import {Badge} from "$lib/components/ui/badge";
    import moment from "moment/moment";

    let {name, global, lastUpdated, updatedBy} = $props<{
        name: string,
        global: boolean,
        lastUpdated: Date,
        updatedBy: string
    }>();
</script>

<div class="mb-4">
    <h2 class="border-none items-center flex space-x-2">
        <span>{name}</span>
        {#if global}
            <Badge variant="secondary">Public</Badge>
        {/if}
    </h2>
    <p class="text-muted-foreground text-sm italic">
        Updated: {moment(lastUpdated).fromNow()} by {updatedBy}
    </p>
</div>
