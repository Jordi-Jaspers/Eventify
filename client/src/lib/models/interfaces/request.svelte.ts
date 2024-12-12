interface PasswordRequest {
	newPassword: string;
	confirmPassword: string;
}

interface UpdatePasswordRequest extends PasswordRequest {
	oldPassword: string;
}

interface ForgotPasswordRequest extends PasswordRequest {
	token: string;
}

interface LoginRequest {
	email: string;
	password: string;
}

interface TeamRequest {
	name: string;
	description: string;
}

interface RefreshTokenRequest {
	refreshToken: string;
}

interface RegisterRequest {
	firstName: string;
	lastName: string;
	email: string;
	password: string;
	passwordConfirmation: string;
}

interface UpdateUserDetailsRequest {
	firstName: string;
	lastName: string;
}

interface UpdateEmailRequest {
	email: string;
}

interface TeamMemberRequest {
	userIds: number[];
}

interface UpdateAuthorityRequest {
	authority: string;
}
