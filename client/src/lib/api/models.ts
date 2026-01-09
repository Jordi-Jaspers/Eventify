import type { components } from '$lib/types/api';

// ================ Authentication ===================
export type LoginRequest = components['schemas']['LoginRequest'];
export type RegisterRequest = components['schemas']['RegisterUserRequest'];
export type AuthenticationResponse = components['schemas']['AuthenticationResponse'];
export type RegisterResponse = components['schemas']['RegisterResponse'];

// ================ User ===================
export type UserResponse = components['schemas']['UserResponse'];
export type UserDetailsResponse = components['schemas']['UserDetailsResponse'];
export type UserOrganizationResponse = components['schemas']['UserOrganizationResponse'];
export type UserQuotaResponse = components['schemas']['UserQuotaResponse'];

// ================ Error Responses ===================
export type ErrorResponseResource = components['schemas']['ErrorResponseResource'];
export type ApiErrorResponseResource = components['schemas']['ApiErrorResponseResource'];
export type ValidationErrorResponseResource = components['schemas']['ValidationErrorResponseResource'];
export type ValidationErrorResource = components['schemas']['ValidationErrorResource'];

// ================ Organization ===================
export type OrganizationResponse = components['schemas']['OrganizationResponse'];
export type ProvisionOrganizationRequest = components['schemas']['ProvisionOrganizationRequest'];

// ================ Organization Membership ===================
export type OrganizationMembershipResponse = components['schemas']['OrganizationMembershipResponse'];
export type AddMemberRequest = components['schemas']['AddMemberRequest'];
export type UpdateMemberRoleRequest = components['schemas']['UpdateMemberRoleRequest'];
export type TransferOwnershipRequest = components['schemas']['TransferOwnershipRequest'];
export type AssignOwnerRequest = components['schemas']['AssignOwnerRequest'];

// ================ Admin ===================
export type AdminStatsResponse = components['schemas']['AdminStatsResponse'];
export type GrowthDataPoint = components['schemas']['GrowthDataPoint'];

// ================ Dev ===================
export type DevCredentialsResponse = components['schemas']['DevCredentialsResponse'];

// ================ API Keys ===================
export type CreateApiKeyRequest = components['schemas']['CreateApiKeyRequest'];
export type ApiKeyCreationResponse = components['schemas']['ApiKeyCreationResponse'];
export type ApiKeyListResponse = components['schemas']['ApiKeyListResponse'];
export type ApiKeyResponse = components['schemas']['ApiKeyResponse'];

// ================ Pagination ===================
export type SearchInput = components['schemas']['SearchInput'];
export type SortableColumn = components['schemas']['SortableColumn'];
export type SortablePageInput = components['schemas']['SortablePageInput'];
export type PageResourceOrganizationResponse = components['schemas']['PageResourceOrganizationResponse'];
export type PageResourceUserResponse = components['schemas']['PageResourceUserResponse'];
export type PageResourceOrganizationMembershipResponse = components['schemas']['PageResourceOrganizationMembershipResponse'];
export type PageResourceUserDetailsResponse = components['schemas']['PageResourceUserDetailsResponse'];
export type PageResourceApiKeyResponse = components['schemas']['PageResourceApiKeyResponse'];

// ================ Enums ===================
// These are inline enums from generated types - extracted for convenience
export type OAuthProvider = 'google' | 'github';
export type OrganizationStatus = NonNullable<OrganizationResponse['status']>;
export type OrganizationalRole = NonNullable<OrganizationMembershipResponse['role']>;
export type UserRole = NonNullable<UserDetailsResponse['role']>;
export type SortDirection = NonNullable<SortableColumn['direction']>;

// ================ Generic PageResource ===================
// Generic type for paginated responses (mirrors backend PageResource<T>)
export interface PageResource<T> {
	totalElements?: number;
	totalPages?: number;
	pageSize?: number;
	pageNumber?: number;
	content?: T[];
}
