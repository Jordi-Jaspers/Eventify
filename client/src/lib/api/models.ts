import type { components } from '$lib/types/api';

// ================ Authentication ===================
export type LoginRequest = components['schemas']['LoginRequest'];
export type RegisterRequest = components['schemas']['RegisterUserRequest'];
export type AuthenticationResponse = components['schemas']['AuthenticationResponse'];
export type RegisterResponse = components['schemas']['RegisterResponse'];
export type ForgotPasswordRequest = components['schemas']['ForgotPasswordRequest'];

// ================ User ===================
export type UserResponse = components['schemas']['UserResponse'];
export type UserDetailsResponse = components['schemas']['UserDetailsResponse'];
export type UserOrganizationResponse = components['schemas']['UserOrganizationResponse'];
export type UserQuotaResponse = components['schemas']['UserQuotaResponse'];
export type RetentionSettingsResponse = components['schemas']['RetentionSettingsResponse'];
export type SessionResponse = components['schemas']['SessionResponse'];
export type ProviderResponse = components['schemas']['ProviderResponse'];

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
export type AdminApiKeyStatsResponse = components['schemas']['ApiKeyStatsResponse'];
export type AdminApiKeyAuditResponse = components['schemas']['ApiKeyAuditResponse'];
export type PageResourceAdminApiKeyAuditResponse =
	components['schemas']['PageResourceApiKeyAuditResponse'];

// ================ Dev ===================
export type DevCredentialsResponse = components['schemas']['DevCredentialsResponse'];

// ================ API Keys ===================
export type CreateApiKeyRequest = components['schemas']['CreateApiKeyRequest'];
export type ApiKeyCreationResponse = components['schemas']['ApiKeyCreationResponse'];
export type ApiKeyListResponse = components['schemas']['ApiKeyListResponse'];
export type ApiKeyResponse = components['schemas']['ApiKeyResponse'];

// ================ Channels ===================
export type ChannelDetailsResponse = components['schemas']['ChannelDetailsResponse'];
export type CreateChannelRequest = components['schemas']['CreateChannelRequest'];
export type UpdateChannelRequest = components['schemas']['UpdateChannelRequest'];
export type PageResourceChannelDetailsResponse =
	components['schemas']['PageResourceChannelDetailsResponse'];

// ================ Watchlists ===================
export type WatchlistDetailsResponse = components['schemas']['WatchlistDetailsResponse'];
export type WatchlistConfigurationResponse =
	components['schemas']['WatchlistConfigurationResponse'];
export type WatchlistFiltersResponse = components['schemas']['WatchlistFiltersResponse'];
export type CreateWatchlistRequest = components['schemas']['CreateWatchlistRequest'];
export type UpdateWatchlistRequest = components['schemas']['UpdateWatchlistRequest'];
export type WatchlistConfigurationRequest = components['schemas']['WatchlistConfigurationRequest'];
export type WatchlistFiltersRequest = components['schemas']['WatchlistFiltersRequest'];
export type PageResourceWatchlistDetailsResponse =
	components['schemas']['PageResourceWatchlistDetailsResponse'];

// ================ Monitor ===================
export type MonitorRequest = components['schemas']['MonitorRequest'];
export type MonitorResponse = components['schemas']['MonitorResponse'];
export type MonitorFilters = components['schemas']['MonitorFilters'];
export type DashboardResponse = components['schemas']['DashboardResponse'];
export type DashboardStatsResponse = components['schemas']['DashboardStatsResponse'];
export type ChannelResponse = components['schemas']['ChannelResponse'];
export type ChannelGroupResponse = components['schemas']['ChannelGroupResponse'];
export type Timeline = components['schemas']['Timeline'];
export type TimelineDuration = components['schemas']['TimelineDuration'];
export type EventSearchResponse = components['schemas']['EventSearchResponse'];
export type PageResourceEventSearchResponse = components['schemas']['PageResourceEventSearchResponse'];
export type TimeRange = '2h' | '4h' | '12h' | '24h' | '7d' | '30d' | 'custom';
export type Severity = 'CRITICAL' | 'WARNING' | 'OK' | 'NO_DATA';
export type BucketSize = NonNullable<MonitorResponse['bucketSize']>;

// ================ Pagination ===================
export type SearchInput = components['schemas']['SearchInput'];
export type SortableColumn = components['schemas']['SortableColumn'];
export type SortablePageInput = components['schemas']['SortablePageInput'];
export type PageResourceOrganizationResponse = components['schemas']['PageResourceOrganizationResponse'];
export type PageResourceUserResponse = components['schemas']['PageResourceUserResponse'];
export type PageResourceOrganizationMembershipResponse = components['schemas']['PageResourceOrganizationMembershipResponse'];
export type PageResourceUserDetailsResponse = components['schemas']['PageResourceUserDetailsResponse'];
export type PageResourceApiKeyResponse = components['schemas']['PageResourceApiKeyResponse'];

// ================ User Notifications ===================
export type NotificationResponse = components['schemas']['NotificationResponse'];
export type PageResourceNotificationResponse = components['schemas']['PageResourceNotificationResponse'];
// ================ Notifications / Broadcasts ===================
export type BroadcastResponseRaw = components['schemas']['BroadcastResponse'];
export type RecipientResponse = components['schemas']['RecipientResponse'];
export type CreateBroadcastRequest = components['schemas']['CreateBroadcastRequest'];
export type AudienceRequest = components['schemas']['AudienceRequest'];
export type PreviewResponse = components['schemas']['PreviewResponse'];
export type PageResourceBroadcastResponse = components['schemas']['PageResourceBroadcastResponse'];
export type PageResourceRecipientResponse = components['schemas']['PageResourceRecipientResponse'];

export type BroadcastCategory = 'ANNOUNCEMENT' | 'SYSTEM' | 'ALERT';
export type AudienceType = 'ALL_USERS' | 'ORGANIZATION' | 'ALL_ORGANIZATION_OWNERS' | 'USER' | 'GLOBAL_ROLE';

// Narrowed type with proper enums (generated schema uses plain strings)
export type BroadcastResponse = Omit<BroadcastResponseRaw, 'category' | 'audienceType'> & {
	category: BroadcastCategory;
	audienceType: AudienceType;
	audienceTargetId?: number;
	audienceRole?: string;
	audienceTargetName?: string;
};

// ================ Enums ===================
// These are inline enums from generated types - extracted for convenience
export type OAuthProvider = 'google' | 'github';
export type ProviderType = NonNullable<ProviderResponse['provider']>;
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
