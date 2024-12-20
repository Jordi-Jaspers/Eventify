export class Dashboards {
	_dashboards: DashboardResponse[] = $state([]);

	getDashboards(): DashboardResponse[] {
		return this._dashboards;
	}

	setDashboards(dashboards: DashboardResponse[]): void {
		this._dashboards = dashboards;
	}

	addDashboard(dashboard: DashboardResponse): void {
		this._dashboards = [...this._dashboards, dashboard];
	}

	removeDashboard(id: number): void {
		this._dashboards = this._dashboards.filter((dashboard) => dashboard.id !== id);
	}

	updateDashboard(id: number, updatedDashboard: DashboardResponse): void {
		this._dashboards = this._dashboards.map((dashboard) => (dashboard.id === id ? updatedDashboard : dashboard));
	}

	clear(): void {
		this._dashboards = [];
	}
}
