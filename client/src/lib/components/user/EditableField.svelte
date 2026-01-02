<script lang="ts">
    import { Input } from '$lib/components/ui/input';
    import { Pencil } from '@lucide/svelte';

    interface Props {
        label: string;
        value: string;
        editing: boolean;
        saving: boolean;
        tempValue: string;
        onStartEdit: () => void;
        onSave: () => void;
        onTempValueChange: (value: string) => void;
        onKeydown: (event: KeyboardEvent) => void;
    }

    let {
        label,
        value,
        editing,
        saving,
        tempValue,
        onStartEdit,
        onSave,
        onTempValueChange,
        onKeydown
    }: Props = $props();

    function handleInput(event: Event): void {
        const target = event.target as HTMLInputElement;
        onTempValueChange(target.value);
    }
</script>

<div class="p-4 rounded-lg bg-background/50 border border-border/50 group relative">
    <p class="text-xs text-muted-foreground mb-1">{label}</p>
    {#if editing}
        <div class="flex items-center gap-2">
            <Input
                value={tempValue}
                oninput={handleInput}
                onkeydown={onKeydown}
                onblur={onSave}
                disabled={saving}
                class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                autofocus
            />
            {#if saving}
                <div class="h-4 w-4 rounded-full border-2 border-primary border-t-transparent animate-spin"></div>
            {/if}
        </div>
    {:else}
        <button
            onclick={onStartEdit}
            class="w-full text-left font-medium text-foreground hover:text-primary transition-colors flex items-center justify-between"
        >
            <span>{value || 'N/A'}</span>
            <Pencil class="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity text-muted-foreground" />
        </button>
    {/if}
</div>
