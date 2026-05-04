<script lang="ts">
    import type { Snippet } from 'svelte';
    import * as AlertDialog from '$lib/components/ui/alert-dialog';

    interface Props {
        open: boolean;
        title: string;
        confirmLabel?: string;
        cancelLabel?: string;
        destructive?: boolean;
        onOpenChange: (open: boolean) => void;
        onConfirm: () => void;
        description?: Snippet;
    }

    let {
        open,
        title,
        confirmLabel = 'Confirm',
        cancelLabel = 'Cancel',
        destructive = false,
        onOpenChange,
        onConfirm,
        description
    }: Props = $props();
</script>

<AlertDialog.Root {open} {onOpenChange}>
    <AlertDialog.Portal>
        <AlertDialog.Overlay />
        <AlertDialog.Content class="bg-card/95 backdrop-blur-xl border-border/50">
            <AlertDialog.Header>
                <AlertDialog.Title>{title}</AlertDialog.Title>
                {#if description}
                    <AlertDialog.Description>
                        {@render description()}
                    </AlertDialog.Description>
                {/if}
            </AlertDialog.Header>
            <AlertDialog.Footer>
                <AlertDialog.Cancel>{cancelLabel}</AlertDialog.Cancel>
                <AlertDialog.Action
                    class={destructive
                        ? 'bg-destructive text-destructive-foreground hover:bg-destructive/90'
                        : ''}
                    onclick={onConfirm}
                >
                    {confirmLabel}
                </AlertDialog.Action>
            </AlertDialog.Footer>
        </AlertDialog.Content>
    </AlertDialog.Portal>
</AlertDialog.Root>
