export function computeDaysFromRange(
	selectedDays: number,
	isCustomRange: boolean,
	customStart: string,
	customEnd: string
): number {
	if (!isCustomRange) return selectedDays;
	const start: Date = new Date(customStart);
	const end: Date = new Date(customEnd);
	const diff: number = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
	return Math.max(1, diff);
}
