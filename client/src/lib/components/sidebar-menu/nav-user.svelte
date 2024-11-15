<script lang="ts">
	import * as Avatar from '$lib/components/ui/avatar';
	import {Root, Trigger, Content, Label, Separator, Group, Item} from '$lib/components/ui/dropdown-menu';
	import {Menu, MenuItem, MenuButton} from '$lib/components/ui/sidebar';
	import { useSidebar } from '$lib/components/ui/sidebar';
	import { toggleMode } from 'mode-watcher';
	import BadgeCheck from 'lucide-svelte/icons/badge-check';
	import Bell from 'lucide-svelte/icons/bell';
	import ChevronsUpDown from 'lucide-svelte/icons/chevrons-up-down';
	import CreditCard from 'lucide-svelte/icons/credit-card';
	import LogOut from 'lucide-svelte/icons/log-out';
	import Sun from 'lucide-svelte/icons/sun';
	import Moon from 'lucide-svelte/icons/moon';

	let { user }: { user: { name: string; email: string; avatar: string } } = $props();
	const sidebar = useSidebar();
</script>

<Menu>
	<MenuItem>
		<Root>
			<Trigger>
				{#snippet child({ props })}
					<MenuButton
						size="lg"
						class="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
						{...props}
					>
						<Avatar.Root class="h-8 w-8 rounded-lg">
							<Avatar.Fallback class="rounded-lg">CN</Avatar.Fallback>
						</Avatar.Root>
						<div class="grid flex-1 text-left text-sm leading-tight">
							<span class="truncate font-semibold">{user.name}</span>
							<span class="truncate text-xs">{user.email}</span>
						</div>
						<ChevronsUpDown class="ml-auto size-4" />
					</MenuButton>
				{/snippet}
			</Trigger>
			<Content
				class="w-[--bits-dropdown-menu-anchor-width] min-w-56 rounded-lg"
				side={sidebar.isMobile ? "bottom" : "right"}
				align="end"
				sideOffset={4}
			>
				<Label class="p-0 font-normal">
					<div class="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
						<Avatar.Root class="h-8 w-8 rounded-lg">
							<Avatar.Fallback class="rounded-lg">CN</Avatar.Fallback>
						</Avatar.Root>
						<div class="grid flex-1 text-left text-sm leading-tight">
							<span class="truncate font-semibold">{user.name}</span>
							<span class="truncate text-xs">{user.email}</span>
						</div>
					</div>
				</Label>
				<Separator />
				<Group>
					<Item>
						<BadgeCheck />
						Account
					</Item>
				</Group>
				<Separator />
				<Group>
					<Item onclick={toggleMode}>
						<span class="gap-2 items-center flex justify-center">
							<Moon class="dark:flex hidden h-[1.2rem] w-[1.2rem] dark:scale-100 transition-all" />
							<Sun class="flex dark:hidden h-[1.2rem] w-[1.2rem] scale-100 transition-all" />
							<span>Switch Mode</span>
						</span>
					</Item>
					<Item>
						<Bell />
						Notifications
					</Item>
					<Item>
						<CreditCard />
						Billing
					</Item>
				</Group>
				<Separator />
				<Item class="data-[highlighted]:bg-red-400/80">
					<LogOut />
					Log out
				</Item>
			</Content>
		</Root>
	</MenuItem>
</Menu>
