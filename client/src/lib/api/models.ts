import type {components} from "$lib/types/api";

export type LoginRequest = components['schemas']['LoginRequest'];
export type RegisterRequest = components['schemas']['RegisterUserRequest'];
export type AuthenticationResponse = components['schemas']['AuthenticationResponse'];
export type RegisterResponse = components['schemas']['RegisterResponse'];

export type UserResponse = components['schemas']['UserResponse'];
export type UserDetailsResponse = components['schemas']['UserDetailsResponse'];
export type UserOrganizationResponse = components['schemas']['UserOrganizationResponse'];

export type ErrorResponseResource = components['schemas']['ErrorResponseResource'];
export type ApiErrorResponseResource = components['schemas']['ApiErrorResponseResource'];
export type ValidationErrorResponseResource = components['schemas']['ValidationErrorResponseResource'];
export type ValidationErrorResource = components['schemas']['ValidationErrorResource'];

export type ProvisionOrganizationRequest = components['schemas']['ProvisionOrganizationRequest'];

export type OrganizationResponse = components['schemas']['OrganizationResponse'];

export type AdminStatsResponse = components['schemas']['AdminStatsResponse'];
export type GrowthDataPoint = components['schemas']['GrowthDataPoint'];

export type SearchInput = components['schemas']['SearchInput'];
export type SortableColumn = components['schemas']['SortableColumn'];
export type SortablePageInput = components['schemas']['SortablePageInput'];

// ================ Enums type ===================
export type OAuthProvider = 'google' | 'github';
export type OrganizationStatus = 'TRIAL' | 'ACTIVE' | 'SUSPENDED';
export type SortDirection = 'ASC' | 'DESC';
export type OrganizationalRole = 'OWNER' | 'ADMIN' | 'MEMBER';

// ================ Generic PageResource type ===================
export interface PageResource<T> {
	totalElements: number;
	totalPages: number;
	pageSize: number;
	pageNumber: number;
	content?: T[];
}

export type PageResourceOrganizationResponse = PageResource<OrganizationResponse>;
export type PageResourceUserResponse = PageResource<UserResponse>;

// ================ Organization Membership types ===================
export interface OrganizationMembershipResponse {
	id: number;
	organizationId: number;
	userId: number;
	userEmail: string;
	userFirstName: string;
	userLastName: string;
	role: OrganizationalRole;
	joinedAt: string;
}

export interface AddMemberRequest {
	email: string;
	role: OrganizationalRole;
}

export interface UpdateMemberRoleRequest {
	role: OrganizationalRole;
}

export interface TransferOwnershipRequest {
	currentOwnerUserId: number;
	newOwnerUserId: number;
}

// UserSearchResult with id field (for search results)
// Note: Backend doesn't return id in search, so we use email as unique identifier
export interface UserSearchResult {
	id?: number; // Optional since backend doesn't always return it
	email?: string;
	firstName?: string;
	lastName?: string;
	enabled?: boolean;
	validated?: boolean;
	role?: 'USER' | 'ADMIN';
}

// ================ Dev Credentials type ===================
export interface DevCredentialsResponse {
	email: string;
	password: string;
}

// ================ Admin Organization types ===================
export interface AssignOwnerRequest {
	email?: string;
	userId?: number;
}
