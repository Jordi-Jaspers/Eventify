<script lang="ts">
	import { LoginForm } from '$lib/components/login';
	import { Tabs, TabsContent, TabsList, TabsTrigger } from '$lib/components/ui/tabs';
	import { RegisterForm } from '$lib/components/register/index.js';
	import { Card } from '$lib/components/ui/card';
	import { Activity } from 'lucide-svelte';
	import { HeartBeat } from '$lib/components/general';
</script>

<div class="flex h-full flex-col">
	<div class="flex p-4">
		<Activity class="mr-2 inline-block h-6 w-6" />
		<h4>Eventify.io</h4>
	</div>

	<div class="flex h-full w-full items-center justify-center px-4">
		<div class="w-full max-w-md">
			<HeartBeat />
			<Tabs value="login" class="w-full space-y-4">
				<TabsList class="grid w-full grid-cols-2">
					<TabsTrigger value="login">Login</TabsTrigger>
					<TabsTrigger value="register">Register</TabsTrigger>
				</TabsList>
				<Card class="snake">
					<TabsContent value="login">
						<LoginForm />
					</TabsContent>
					<TabsContent value="register">
						<RegisterForm />
					</TabsContent>
				</Card>
			</Tabs>
		</div>
	</div>
</div>
