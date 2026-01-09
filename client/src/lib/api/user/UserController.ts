import {client} from "$lib/api/client.ts";
import type {UserDetailsResponse, UserOrganizationResponse, UserQuotaResponse} from "$lib/api/models.ts";

/**
 * Get current user details (full profile info)
 */
export async function getUserDetails(): Promise<UserDetailsResponse> {
    const {data, error} = await client.GET('/v1/user/details');

    if (error) {
        throw error;
    }

    return data;
}

/**
 * Update user details (first name and last name)
 */
export async function updateUserDetails(firstName: string, lastName: string): Promise<UserDetailsResponse> {
    const {data, error} = await client.POST('/v1/user/details', {
        body: {
            firstName,
            lastName
        }
    });

    if (error) {
        throw error;
    }

    return data;
}

/**
 * Get all organizations for the authenticated user
 */
export async function getUserOrganizations(): Promise<UserOrganizationResponse[]> {
    const {data, error} = await client.GET('/v1/user/organization');

    if (error) {
        throw error;
    }

    return data;
}

/**
 * Get user quota information (API usage limits)
 */
export async function getQuota(): Promise<UserQuotaResponse> {
    const {data, error} = await client.GET('/v1/user/quota');

    if (error) {
        throw error;
    }

    return data;
}
