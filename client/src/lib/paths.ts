// Base URL for the API
export const SERVER_BASE_URL: string = import.meta.env.VITE_SERVER_BASE_URL + '/api';
export const SERVER_URLS = {
	SERVER_BASE_URL,
	AUTH_PATH: `${SERVER_BASE_URL}/auth`,
	REGISTER_PATH: `${SERVER_BASE_URL}/auth/register`,
	VALIDATE_PATH: `${SERVER_BASE_URL}/auth/validate`,
	AUTHORIZE_PATH: `${SERVER_BASE_URL}/auth/authorize`,
	REFRESH_PATH: `${SERVER_BASE_URL}/auth/token`,
	USER_PATH: `${SERVER_BASE_URL}/user`,
	USER_DETAILS_PATH: `${SERVER_BASE_URL}/user/details`,
	USER_DETAILS_EMAIL_PATH: `${SERVER_BASE_URL}/user/details/email`,
	EMAIL_VALIDATION_PATH: `${SERVER_BASE_URL}/user/details/email/validate`,
	REQUEST_PASSWORD_RESET_PATH: `${SERVER_BASE_URL}/public/reset_password/request`,
	PUBLIC_PASSWORD_RESET_PATH: `${SERVER_BASE_URL}/public/reset_password`,
	UPDATE_PASSWORD_PATH: `${SERVER_BASE_URL}/password`
};

export const CLIENT_URLS = {
	LOGIN_URL: '/login',
	FORGOT_PASSWORD_URL: '/password/forgot',
	RESET_PASSWORD_URL: '/password/reset',
	ACCOUNT_URL: '/account'
};
