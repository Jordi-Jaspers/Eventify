# User Channel CRUD (Backend + Frontend)

**Epic**: Event Channels
**Status**: Ready for Dev
**Estimate**: L (Large)
**Created Date**: 2026-01-11

## 1. User Story
**As a** registered user
**I want** to create, view, update, pause, and delete my personal channels
**So that** I can organize my events into logical groupings

## 2. Business Context & Value
Personal channels allow individual users to segment their event streams. A developer might have separate channels for "Backend Errors", "Frontend Errors", and "Deployment Events". This is the core self-service functionality for personal accounts.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: User creates a personal channel
    *   Given I am authenticated
    *   When I create a channel with name "My App Errors" and optional description
    *   Then a new channel is created with scope USER and my user ID as owner
    *   And I receive the channel details in the response

*   [ ] **Scenario 2**: User lists their personal channels
    *   Given I have created 3 personal channels
    *   When I request my channel list
    *   Then I see all 3 channels with their name, status, and created date
    *   And I do not see channels belonging to other users

*   [ ] **Scenario 3**: User updates channel details
    *   Given I have a channel named "Old Name"
    *   When I update the name to "New Name" and add a description
    *   Then the channel is updated successfully
    *   And the updated_at timestamp is set

*   [ ] **Scenario 4**: User pauses a channel
    *   Given I have an active channel
    *   When I pause the channel
    *   Then the channel status becomes PAUSED
    *   And the channel is still visible in my list

*   [ ] **Scenario 5**: User resumes a paused channel
    *   Given I have a paused channel
    *   When I resume the channel
    *   Then the channel status becomes ACTIVE

*   [ ] **Scenario 6**: User deletes a channel
    *   Given I have a channel (active or paused)
    *   When I delete the channel
    *   Then the channel status becomes PENDING_DELETION
    *   And the channel no longer appears in my list
    *   And a background job will clean up the channel and its events

*   [ ] **Scenario 7**: Duplicate channel name rejected
    *   Given I have a channel named "Errors"
    *   When I try to create another channel named "Errors"
    *   Then I receive a 409 Conflict error

*   [ ] **Scenario 8**: Frontend displays channels on dashboard
    *   Given I am on my user dashboard
    *   When the page loads
    *   Then I see a "My Channels" section listing my channels
    *   And I can create, edit, pause/resume, and delete channels from the UI

## 4. Technical Requirements
*   **API Endpoints**:
    | Method | Path | Description |
    |--------|------|-------------|
    | POST | `/v1/user/channels` | Create personal channel |
    | GET | `/v1/user/channels` | List user's channels (paginated) |
    | GET | `/v1/user/channels/{id}` | Get channel details |
    | PUT | `/v1/user/channels/{id}` | Update channel name/description |
    | POST | `/v1/user/channels/{id}/pause` | Pause channel |
    | POST | `/v1/user/channels/{id}/resume` | Resume channel |
    | DELETE | `/v1/user/channels/{id}` | Delete channel (sets PENDING_DELETION) |
*   **Request/Response DTOs**:
    *   `CreateChannelRequest`: { name, description? }
    *   `UpdateChannelRequest`: { name?, description? }
    *   `ChannelResponse`: { id, name, description, status, retentionDays, createdAt, updatedAt }
    *   `ChannelListResponse`: { channels[], pagination }
*   **Authorization**: JWT required, user can only manage their own channels
*   **Validation**:
    *   Name: required, 1-100 chars, unique per user
    *   Description: optional, max 500 chars

## 5. Design & UI/UX
*   **Dashboard Section**: Add "My Channels" card/section to user dashboard
*   **Channel List**: DataTable with columns: Name, Status (badge), Created, Actions
*   **Create Modal**: Simple form with name and description fields
*   **Status Badge**: Green for ACTIVE, Yellow for PAUSED
*   **Actions Dropdown**: Edit, Pause/Resume, Delete
*   **Delete Confirmation**: Modal with warning about permanent deletion

## 6. Implementation Notes / Research
*   **Backend Package**: `io.github.eventify.api.channel`
*   **Follow patterns from**: `api/apikey` (similar CRUD structure)
*   **Controller**: `UserChannelController`
*   **Service**: `ChannelService` (shared between user and org)
*   **Frontend Route**: Add to existing `/dashboard` page or create `/dashboard/channels`
*   **API Client**: Generate or create `ChannelController.ts` in `client/src/lib/api/`
*   **Paths.java**: Add channel path constants (some already exist: `CHANNELS_PATH`)
