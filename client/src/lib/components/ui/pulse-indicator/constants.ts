/**
 * Size variants for the PulseIndicator component
 */
export type PulseIndicatorSize = 'xs' | 'sm' | 'md' | 'lg';

/**
 * Color variants for the PulseIndicator component
 */
export type PulseIndicatorVariant = 'green' | 'primary' | 'red' | 'yellow' | 'blue';

/**
 * Tailwind CSS classes for different indicator sizes
 */
export const SIZE_CLASSES: Record<PulseIndicatorSize, string> = {
	xs: 'h-1.5 w-1.5',
	sm: 'h-2 w-2',
	md: 'h-2.5 w-2.5',
	lg: 'h-3 w-3'
} as const;

/**
 * Tailwind CSS classes for different indicator variants (background colors)
 */
export const VARIANT_CLASSES: Record<PulseIndicatorVariant, string> = {
	green: 'bg-green-500',
	primary: 'bg-primary',
	red: 'bg-red-500',
	yellow: 'bg-yellow-500',
	blue: 'bg-blue-500'
} as const;

/**
 * Tailwind CSS classes for label text colors matching each variant
 */
export const LABEL_COLOR_CLASSES: Record<PulseIndicatorVariant, string> = {
	green: 'text-green-500',
	primary: 'text-primary',
	red: 'text-red-500',
	yellow: 'text-yellow-500',
	blue: 'text-blue-500'
} as const;
