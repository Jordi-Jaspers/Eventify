import type {components} from "$lib/types/api";

export type OAuthProvider = 'google' | 'github';

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

// Organization listing types
export type OrganizationStatus = 'TRIAL' | 'ACTIVE' | 'SUSPENDED';

// PageResponse wrapper for paginated results
export interface PageResponseOrganizationResponse {
	content?: OrganizationResponse[];
	pageNumber?: number;
	pageSize?: number;
	totalElements?: number;
	totalPages?: number;
	first?: boolean;
	last?: boolean;
}

// Jframe SortablePageInput types (for search endpoints)
export interface SearchInput {
	fieldName: string;
	textValue?: string;
	fromDateValue?: string;
	toDateValue?: string;
	textValueList?: string[];
}

export interface SortableColumn {
	column: string;
	direction: 'ASC' | 'DESC';
}

export interface SortablePageInput {
	pageNumber: number;
	pageSize: number;
	sortOrder?: SortableColumn[];
	searchInputs?: SearchInput[];
}
