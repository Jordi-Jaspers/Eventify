<script lang="ts">
    import {onMount} from 'svelte';
    import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '$lib/components/ui/card';
    import {Alert, AlertDescription} from '$lib/components/ui/alert';
    import Button from '$lib/components/ui/button/button.svelte';
    import * as Chart from '$lib/components/ui/chart';
    import {AreaChart, Tooltip} from 'layerchart';
    import {scaleTime} from 'd3-scale';
    import {getAdminStats} from '$lib/api/admin/AdminController';
    import {CLIENT_ROUTES} from '$lib/config/routes';
    import {handleError} from '$lib/utils/error-handler';
    import {toast} from 'svelte-sonner';
    import {
        Users,
        Building2,
        Activity,
        TrendingUp,
        CircleAlert,
        Plus,
        UserCog,
        Building
    } from '@lucide/svelte';
    import type {AdminStatsResponse, GrowthDataPoint} from '$lib/api/models.ts';
    import type {ChartConfig} from '$lib/components/ui/chart/types';

    let stats: AdminStatsResponse | null = $state(null);
    let loading: boolean = $state(true);
    let error: string | null = $state(null);

    const chartConfig: ChartConfig = {
        totalOrganizations: {
            label: 'Organizations',
            color: 'hsl(280 80% 60%)'  // Purple
        },
        totalUsers: {
            label: 'Users',
            color: 'hsl(210 80% 60%)'  // Blue
        }
    };

    async function loadStats(): Promise<void> {
        loading = true;
        error = null;

        try {
            stats = await getAdminStats();
        } catch (err: unknown) {
            const {message}: { message: string } = handleError(err, 'Failed to load admin statistics');
            error = message;
            toast.error(message);
        } finally {
            loading = false;
        }
    }

    onMount(() => {
        loadStats();
    });

    function getActiveUserPercentage(): number {
        if (!stats || !stats.totalUsers || stats.totalUsers === 0) return 0;
        return Math.round(((stats.activeUsers || 0) / stats.totalUsers) * 100);
    }

    interface ChartDataPoint {
        date: Date;
        totalOrganizations: number;
        totalUsers: number;
        dateStr: string;
    }

    function formatChartData(growthData: GrowthDataPoint[]): ChartDataPoint[] {
        return growthData.map((point: GrowthDataPoint) => {
            const dateStr: string = point.date ?? '';
            const date: Date = new Date(dateStr);
            const month: string = date.toLocaleDateString('en-US', {month: 'short'});
            const day: string = date.getDate().toString();
            return {
                date,
                totalOrganizations: point.totalOrganizations || 0,
                totalUsers: point.totalUsers || 0,
                dateStr: `${month} ${day}`
            };
        });
    }

    function formatXAxisDate(date: Date): string {
        const month: string = date.toLocaleDateString('en-US', {month: 'short'});
        const day: string = date.getDate().toString();
        return `${month} ${day}`;
    }

    function formatTooltipDate(date: Date): string {
        return date.toLocaleDateString('en-US', {
            month: 'long',
            day: 'numeric',
            year: 'numeric'
        });
    }
</script>

<svelte:head>
    <title>Admin Dashboard - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
    <div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
        <!-- Header -->
        <div class="mb-8">
            <h1
                    class="text-3xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent"
            >
                Admin Dashboard
            </h1>
            <p class="text-muted-foreground mt-2">Platform statistics and management overview</p>
        </div>

        <!-- Error Alert -->
        {#if error && !loading}
            <Alert
                    variant="destructive"
                    class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm"
            >
                <CircleAlert class="h-4 w-4"/>
                <AlertDescription>
                    {error}
                    <Button variant="outline" size="sm" class="ml-4" onclick={loadStats}> Retry</Button>
                </AlertDescription>
            </Alert>
        {/if}

        <!-- Stats Cards -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- Total Organizations Card -->
            <Card
                    class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-primary/20 hover:border-primary/50 transition-all duration-300"
            >
                <div
                        class="absolute inset-0 bg-gradient-to-br from-purple-500/10 via-transparent to-purple-500/5 opacity-50"
                ></div>
                <CardHeader class="relative z-10">
                    <div class="flex items-center justify-between">
                        <CardTitle class="text-sm font-medium text-muted-foreground"
                        >Total Organizations
                        </CardTitle
                        >
                        <Building2 class="h-5 w-5 text-purple-500"/>
                    </div>
                </CardHeader>
                <CardContent class="relative z-10">
                    {#if loading}
                        <div class="h-12 bg-muted/50 rounded animate-pulse"></div>
                    {:else}
                        <div
                                class="text-3xl font-bold bg-gradient-to-r from-purple-500 to-purple-400 bg-clip-text text-transparent"
                        >
                            {stats?.totalOrganizations?.toLocaleString() || '0'}
                        </div>
                    {/if}
                </CardContent>
            </Card>

            <!-- Total Users Card -->
            <Card
                    class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-blue-500/20 hover:border-blue-500/50 transition-all duration-300"
            >
                <div
                        class="absolute inset-0 bg-gradient-to-br from-blue-500/10 via-transparent to-blue-500/5 opacity-50"
                ></div>
                <CardHeader class="relative z-10">
                    <div class="flex items-center justify-between">
                        <CardTitle class="text-sm font-medium text-muted-foreground">Total Users</CardTitle>
                        <Users class="h-5 w-5 text-blue-500"/>
                    </div>
                </CardHeader>
                <CardContent class="relative z-10">
                    {#if loading}
                        <div class="h-12 bg-muted/50 rounded animate-pulse"></div>
                    {:else}
                        <div
                                class="text-3xl font-bold bg-gradient-to-r from-blue-500 to-blue-400 bg-clip-text text-transparent"
                        >
                            {stats?.totalUsers?.toLocaleString() || '0'}
                        </div>
                    {/if}
                </CardContent>
            </Card>

            <!-- Active Users Card -->
            <Card
                    class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-green-500/20 hover:border-green-500/50 transition-all duration-300"
            >
                <div
                        class="absolute inset-0 bg-gradient-to-br from-green-500/10 via-transparent to-green-500/5 opacity-50"
                ></div>
                <CardHeader class="relative z-10">
                    <div class="flex items-center justify-between">
                        <CardTitle class="text-sm font-medium text-muted-foreground">Active Users</CardTitle>
                        <Activity class="h-5 w-5 text-green-500"/>
                    </div>
                </CardHeader>
                <CardContent class="relative z-10">
                    {#if loading}
                        <div class="h-12 bg-muted/50 rounded animate-pulse"></div>
                    {:else}
                        <div class="flex items-baseline gap-2">
                            <div
                                    class="text-3xl font-bold bg-gradient-to-r from-green-500 to-green-400 bg-clip-text text-transparent"
                            >
                                {stats?.activeUsers?.toLocaleString() || '0'}
                            </div>
                            <div class="text-sm text-muted-foreground">({getActiveUserPercentage()}%)</div>
                        </div>
                    {/if}
                </CardContent>
            </Card>
        </div>

        <!-- Growth Chart -->
        <Card
                class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden"
        >
            <div
                    class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"
            ></div>
            <CardHeader class="relative z-10">
                <div class="flex items-center gap-2">
                    <TrendingUp class="w-5 h-5 text-primary"/>
                    <CardTitle class="text-xl">Growth Trends</CardTitle>
                </div>
                <CardDescription>Cumulative growth of organizations and users over the last 30 days</CardDescription>
            </CardHeader>
            <CardContent class="relative z-10">
                {#if loading}
                    <div class="h-80 bg-muted/50 rounded animate-pulse"></div>
                {:else if stats?.growthData && stats.growthData.length > 0}
                    <Chart.Container config={chartConfig} class="h-80">
                        <AreaChart
                                data={formatChartData(stats.growthData)}
                                x="date"
                                xScale={scaleTime()}
                                series={[
                                    {
                                        key: 'totalOrganizations',
                                        label: 'Organizations',
                                        value: 'totalOrganizations',
                                        color: chartConfig.totalOrganizations.color
                                    },
                                    {
                                        key: 'totalUsers',
                                        label: 'Users',
                                        value: 'totalUsers',
                                        color: chartConfig.totalUsers.color
                                    }
                                ]}
                                seriesLayout="overlap"
                                padding={{ top: 20, bottom: 60, left: 60, right: 20 }}
                                props={{
                                    area: {
                                        fillOpacity: 0.2
                                    },
                                    xAxis: {
                                        format: formatXAxisDate,
                                        rule: true
                                    },
                                    yAxis: {
                                        rule: true
                                    }
                                }}
                        >
                            {#snippet tooltip()}
                                <Tooltip.Root variant="none">
                                    {#snippet children({ data }: { data: ChartDataPoint })}
                                        <div
                                                class="rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-xl p-3"
                                        >
                                            <div class="font-semibold text-sm mb-2">
                                                {formatTooltipDate(data.date)}
                                            </div>
                                            <div class="space-y-1.5">
                                                <div class="flex items-center gap-2">
                                                    <div
                                                            class="h-2.5 w-2.5 rounded-full"
                                                            style="background-color: {chartConfig.totalOrganizations.color}"
                                                    ></div>
                                                    <span class="text-xs text-muted-foreground">Organizations:</span>
                                                    <span class="text-sm font-medium ml-auto"
                                                    >{data.totalOrganizations.toLocaleString()}</span
                                                    >
                                                </div>
                                                <div class="flex items-center gap-2">
                                                    <div
                                                            class="h-2.5 w-2.5 rounded-full"
                                                            style="background-color: {chartConfig.totalUsers.color}"
                                                    ></div>
                                                    <span class="text-xs text-muted-foreground">Users:</span>
                                                    <span class="text-sm font-medium ml-auto"
                                                    >{data.totalUsers.toLocaleString()}</span
                                                    >
                                                </div>
                                            </div>
                                        </div>
                                    {/snippet}
                                </Tooltip.Root>
                            {/snippet}
                        </AreaChart>
                    </Chart.Container>
                {:else}
                    <div class="h-80 flex items-center justify-center text-muted-foreground">
                        <div class="text-center">
                            <TrendingUp class="h-12 w-12 mx-auto mb-4 opacity-50"/>
                            <p>No growth data available</p>
                        </div>
                    </div>
                {/if}
            </CardContent>
        </Card>

        <!-- Quick Actions -->
        <Card
                class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden"
        >
            <div
                    class="absolute inset-0 bg-gradient-to-br from-accent/10 via-transparent to-primary/10 opacity-50"
            ></div>
            <CardHeader class="relative z-10">
                <CardTitle class="text-xl">Quick Actions</CardTitle>
                <CardDescription>Manage platform resources</CardDescription>
            </CardHeader>
            <CardContent class="relative z-10">
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <!-- Create Organization -->
                    <Button
                            class="bg-gradient-to-r from-primary to-accent hover:opacity-90 transition-all shadow-lg hover:shadow-primary/50 h-auto py-4"
                            onclick={() => (window.location.href = CLIENT_ROUTES.ADMIN_ORGANIZATIONS_NEW.path)}
                    >
                        <div class="flex flex-col items-center gap-2">
                            <Plus class="h-5 w-5"/>
                            <span>Create Organization</span>
                        </div>
                    </Button>

                    <!-- Manage Users (Future) -->
                    <Button
                            variant="outline"
                            class="bg-background/50 border-border/50 hover:bg-primary/10 transition-all h-auto py-4"
                            disabled
                            title="Coming soon"
                    >
                        <div class="flex flex-col items-center gap-2">
                            <UserCog class="h-5 w-5"/>
                            <span>Manage Users</span>
                            <span class="text-xs text-muted-foreground">(Coming Soon)</span>
                        </div>
                    </Button>

                    <!-- Manage Organizations (Future) -->
                    <Button
                            variant="outline"
                            class="bg-background/50 border-border/50 hover:bg-primary/10 transition-all h-auto py-4"
                            disabled
                            title="Coming soon"
                    >
                        <div class="flex flex-col items-center gap-2">
                            <Building class="h-5 w-5"/>
                            <span>Manage Organizations</span>
                            <span class="text-xs text-muted-foreground">(Coming Soon)</span>
                        </div>
                    </Button>
                </div>
            </CardContent>
        </Card>
    </div>
</main>
