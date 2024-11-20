<script lang="ts">
	import { LoginForm } from '$lib/components/login';
	import { Tabs, TabsContent, TabsList, TabsTrigger } from '$lib/components/ui/tabs';
	import { RegisterForm } from '$lib/components/register';
	import { Card } from '$lib/components/ui/card';
	import {HeartBeat, Logo} from '$lib/components/general';
</script>

<div class="flex h-full flex-col">
	<Logo/>
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
