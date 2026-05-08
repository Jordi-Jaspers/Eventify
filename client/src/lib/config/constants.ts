/* ==================== API CONFIGURATION ==================== */
// If VITE_SERVER_BASE_URL is not set or empty, use relative path (same-origin)
// This allows the frontend to work with Traefik routing where /api/* goes to backend
const baseUrl = import.meta.env.VITE_SERVER_BASE_URL || '';
export const SERVER_BASE_URL: string = baseUrl ? `${baseUrl}/api` : '/api';

/* ==================== COOKIE CONFIGURATION ==================== */
export const ACCESS_TOKEN_COOKIE = 'EVENTIFY_ACCESS_TOKEN';
export const REFRESH_TOKEN_COOKIE = 'EVENTIFY_REFRESH_TOKEN';
