import type {components} from "$lib/types/api";

export type LoginRequest = components['schemas']['LoginRequest'];
export type RegisterRequest = components['schemas']['RegisterUserRequest'];
export type AuthenticationResponse = components['schemas']['AuthenticationResponse'];
export type UserDetailsResponse = components['schemas']['UserDetailsResponse'];
export type RegisterResponse = components['schemas']['RegisterResponse'];

export type ErrorResponseResource = components['schemas']['ErrorResponseResource'];
export type ApiErrorResponseResource = components['schemas']['ApiErrorResponseResource'];
export type ValidationErrorResponseResource = components['schemas']['ValidationErrorResponseResource'];
export type ValidationErrorResource = components['schemas']['ValidationErrorResource'];

export type ProvisionOrganizationRequest = components['schemas']['ProvisionOrganizationRequest'];
export type OrganizationResponse = components['schemas']['OrganizationResponse'];

export type AdminStatsResponse = components['schemas']['AdminStatsResponse'];
export type GrowthDataPoint = components['schemas']['GrowthDataPoint'];

export type UserSearchResult = components['schemas']['UserSearchResult'];

export type SearchInput = components['schemas']['SearchInput'];
export type SortableColumn = components['schemas']['SortableColumn'];
export type SortablePageInput = components['schemas']['SortablePageInput'];

// ================ Enums type ===================
export type OAuthProvider = 'google' | 'github';
export type OrganizationStatus = 'TRIAL' | 'ACTIVE' | 'SUSPENDED';
export type SortDirection = 'ASC' | 'DESC';

// ================ Generic PageResource type ===================
export interface PageResource<T> {
	totalElements: number;
	totalPages: number;
	pageSize: number;
	pageNumber: number;
	content?: T[];
}

export type PageResourceOrganizationResponse = PageResource<OrganizationResponse>;
