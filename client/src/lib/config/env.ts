/**
 * Environment Detection and Configuration
 * 
 * Reads PUBLIC_ENVIRONMENT from runtime environment variables
 * and provides type-safe environment helpers.
 * 
 * Environment controls:
 * - 'local': Development mode - shows dev credentials, DEV badge on logo
 * - 'test': Test/staging mode - shows dev credentials, TST badge on logo  
 * - 'production': Production mode - no dev features, no badge
 */
import { env } from '$env/dynamic/public';

export type Environment = 'local' | 'test' | 'production';

/**
 * Get current environment from PUBLIC_ENVIRONMENT variable.
 * Defaults to 'production' if not set or invalid (fail-safe).
 */
export function getEnvironment(): Environment {
	const rawEnv: string | undefined = env.PUBLIC_ENVIRONMENT;
	
	if (rawEnv === 'local' || rawEnv === 'test' || rawEnv === 'production') {
		return rawEnv;
	}
	
	// Default to production for safety
	return 'production';
}

/**
 * Check if running in production environment.
 */
export function isProduction(): boolean {
	return getEnvironment() === 'production';
}

/**
 * Check if dev features should be shown (credentials, playbook, etc).
 * Returns true only for non-production environments (local/test).
 */
export function showDevCredentials(): boolean {
	return !isProduction();
}
