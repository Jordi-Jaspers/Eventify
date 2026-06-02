export function formatChartXAxis(date: Date, days: number): string {
	if (days <= 7)
		return date.toLocaleString('en-US', {
			month: 'short',
			day: 'numeric',
			hour: '2-digit',
			minute: '2-digit',
			hour12: false
		});
	if (days <= 30) return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
	return date.toLocaleDateString('en-US', { month: 'short' });
}

export function formatChartTooltipDate(date: Date, days: number): string {
	if (days <= 7)
		return date.toLocaleString('en-US', {
			month: 'short',
			day: 'numeric',
			hour: '2-digit',
			minute: '2-digit',
			hour12: false
		});
	return date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
}
