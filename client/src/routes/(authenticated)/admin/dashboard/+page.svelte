<script lang="ts">
    import {onMount} from 'svelte';
    import {goto} from '$app/navigation';
    import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '$lib/components/ui/card';
    import {Alert, AlertDescription} from '$lib/components/ui/alert';
    import {Badge} from '$lib/components/ui/badge';
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
    import {StatCard} from '$lib/components/admin';

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
        newUsersGrowthPercentage?: number | null;
        newOrganizationsGrowthPercentage?: number | null;
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
                dateStr: `${month} ${day}`,
                newUsersGrowthPercentage: point.newUsersGrowthPercentage,
                newOrganizationsGrowthPercentage: point.newOrganizationsGrowthPercentage
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

    function formatPercentage(value: number | null | undefined): string {
        if (value === null || value === undefined) return '0%';
        const sign: string = value > 0 ? '+' : '';
        return `${sign}${value.toFixed(1)}%`;
    }

    function getBadgeVariant(value: number | null | undefined): 'default' | 'success' | 'destructive' {
        if (value === null || value === undefined || value === 0) return 'default';
        return value > 0 ? 'success' : 'destructive';
    }

    function getLatestGrowth(): GrowthDataPoint | null {
        if (!stats?.growthData || stats.growthData.length === 0) return null;
        return stats.growthData[stats.growthData.length - 1];
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
                    class="text-3xl font-bold text-primary"
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
            <StatCard
                title="Total Organizations"
                value={stats?.totalOrganizations?.toLocaleString() || '0'}
                icon={Building2}
                variant="purple"
                {loading}
            >
                {#snippet trailing()}
                    {#if getLatestGrowth()?.newOrganizationsGrowthPercentage !== null && getLatestGrowth()?.newOrganizationsGrowthPercentage !== undefined}
                        <Badge variant={getBadgeVariant(getLatestGrowth()?.newOrganizationsGrowthPercentage)} class="mb-1.5">
                            {formatPercentage(getLatestGrowth()?.newOrganizationsGrowthPercentage)}
                        </Badge>
                    {/if}
                {/snippet}
            </StatCard>

            <!-- Total Users Card -->
            <StatCard
                title="Total Users"
                value={stats?.totalUsers?.toLocaleString() || '0'}
                icon={Users}
                variant="blue"
                {loading}
            >
                {#snippet trailing()}
                    {#if getLatestGrowth()?.newUsersGrowthPercentage !== null && getLatestGrowth()?.newUsersGrowthPercentage !== undefined}
                        <Badge variant={getBadgeVariant(getLatestGrowth()?.newUsersGrowthPercentage)} class="mb-1.5">
                            {formatPercentage(getLatestGrowth()?.newUsersGrowthPercentage)}
                        </Badge>
                    {/if}
                {/snippet}
            </StatCard>

            <!-- Active Users Card -->
            <StatCard
                title="Active Users"
                value={stats?.activeUsers?.toLocaleString() || '0'}
                icon={Activity}
                variant="green"
                {loading}
            >
                {#snippet trailing()}
                    <div class="text-sm text-muted-foreground mb-1">({getActiveUserPercentage()}%)</div>
                {/snippet}
            </StatCard>
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
                    <!-- Chart -->
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
                                                    {#if data.newOrganizationsGrowthPercentage !== null && data.newOrganizationsGrowthPercentage !== undefined}
                                                        <Badge variant={getBadgeVariant(data.newOrganizationsGrowthPercentage)} class="text-xs px-1.5 py-0">
                                                            {formatPercentage(data.newOrganizationsGrowthPercentage)}
                                                        </Badge>
                                                    {/if}
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
                                                    {#if data.newUsersGrowthPercentage !== null && data.newUsersGrowthPercentage !== undefined}
                                                        <Badge variant={getBadgeVariant(data.newUsersGrowthPercentage)} class="text-xs px-1.5 py-0">
                                                            {formatPercentage(data.newUsersGrowthPercentage)}
                                                        </Badge>
                                                    {/if}
                                                </div>
                                            </div>
                                        </div>
                                    {/snippet}
                                </Tooltip.Root>
                            {/snippet}
                        </AreaChart>
                    </Chart.Container>

                    <!-- Chart Legend -->
                    <div class="flex items-center justify-center gap-6 mt-6 pt-4 border-t border-border/50">
                        <div class="flex items-center gap-2">
                            <div
                                    class="h-3 w-3 rounded-full"
                                    style="background-color: {chartConfig.totalOrganizations.color}"
                            ></div>
                            <span class="text-sm text-muted-foreground">Organizations</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <div
                                    class="h-3 w-3 rounded-full"
                                    style="background-color: {chartConfig.totalUsers.color}"
                            ></div>
                            <span class="text-sm text-muted-foreground">Users</span>
                        </div>
                    </div>
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
                <div class="grid grid-cols-1 md:grid-cols-4 gap-2">
                    <!-- Manage Organizations -->
                    <Button
                            class="h-auto py-4"
                            onclick={() => (window.location.href = CLIENT_ROUTES.ADMIN_ORGANIZATIONS_PAGE.path)}
                    >
                        <div class="flex flex-col items-center gap-2">
                            <Building class="h-5 w-5"/>
                            <span>Manage Organizations</span>
                        </div>
                    </Button>

                    <!-- Create Organization -->
                    <Button
                            class="h-auto py-4"
                            onclick={() => goto(CLIENT_ROUTES.ADMIN_ORGANIZATIONS_PAGE.path)}
                    >
                        <div class="flex flex-col items-center gap-2">
                            <Plus class="h-5 w-5"/>
                            <span>Create Organization</span>
                        </div>
                    </Button>

                    <!-- Manage Users -->
                    <Button
                            class="h-auto py-4"
                            onclick={() => goto(CLIENT_ROUTES.ADMIN_USERS_PAGE.path)}
                    >
                        <div class="flex flex-col items-center gap-2">
                            <UserCog class="h-5 w-5"/>
                            <span>Manage Users</span>
                        </div>
                    </Button>

                    <!-- Manage API keys -->
                    <Button
                            class="h-auto py-4"
                            onclick={() => goto(CLIENT_ROUTES.ADMIN_API_KEYS_PAGE.path)}
                    >
                        <div class="flex flex-col items-center gap-2">
                            <UserCog class="h-5 w-5"/>
                            <span>Manage API Keys</span>
                        </div>
                    </Button>
                </div>
            </CardContent>
        </Card>
    </div>
</main>
