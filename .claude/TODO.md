# Stories
- [x] **Show User Profile**:
    - Create endpoint to get user profile details
    - Frontend page to display user profile information
    - Include fields: username, email, profile picture, bio

- [x] **Edit User Profile**:
    - Create endpoint to update user profile details
    - Frontend page/form to edit profile information
    - Validate input fields (e.g., email format)

- [ ] **Long-Lived Refresh Tokens**:
    - Add a remember-me option during login for longer refresh token validity

- [ ] **Multi-Token Support**:
    - Allow multiple active refresh tokens per user (for different devices)
    - User can retrieve list of active refresh tokens

- [ ] **Token Revocation**:
    - User can revoke specific refresh tokens
    - User can revoke all refresh tokens (logout from all devices, including current one, logout)

- [ ] **OAuth2 Enhancements**:
    - Implement account linking strategy (when user registers with email, then tries OAuth2 with same email)

---

## MVP Features - Event Management

- [ ] **Create Event**:
    - Create event with: name, description, type/category, severity level
    - Timestamp captured automatically
    - Event metadata (key-value pairs)
    - Validate required fields

- [ ] **View Events**:
    - Real-time event dashboard with live updates
    - Event list with pagination
    - Event detail view
    - Display: timestamp, type, severity, description, metadata

- [ ] **Filter and Search Events**:
    - Filter by: date range, event type, severity, status
    - Full-text search in event descriptions
    - Saved filter presets
    - Export filtered results (CSV/JSON)

- [ ] **Event Categories**:
    - Predefined event types (Error, Warning, Info, Success, Custom)
    - User-defined custom categories
    - Color-coded severity levels (Critical, High, Medium, Low)
    - Category-specific icons

- [ ] **Event Timeline**:
    - Chronological event visualization
    - Group events by time periods (hour, day, week)
    - Timeline zoom and navigation
    - Event clustering for high-frequency periods

## MVP Features - Team Collaboration

- [ ] **Create Team**:
    - Team name, description
    - Team owner/creator automatically assigned
    - Team settings and preferences

- [ ] **Invite Team Members**:
    - Email-based invitations
    - Role assignment: Owner, Admin, Member, Viewer
    - Invitation expiration (7 days)
    - Resend invitation option

- [ ] **Team Dashboard**:
    - Shared event view for team
    - Team member list with roles
    - Team activity feed
    - Team-level event filtering

- [ ] **Role-Based Permissions**:
    - Owner: Full control, delete team
    - Admin: Manage members, create/edit events
    - Member: Create/edit own events, view all
    - Viewer: Read-only access

## MVP Features - Notifications & Alerts

- [ ] **Real-Time Notifications**:
    - WebSocket-based live event notifications
    - Browser push notifications
    - Notification badge on navbar
    - In-app notification center

- [ ] **Email Notifications**:
    - Daily event digest email
    - Critical event instant alerts
    - Team invitation emails
    - Weekly summary reports

- [ ] **Notification Preferences**:
    - Enable/disable notification types
    - Quiet hours configuration
    - Event severity threshold settings
    - Delivery channel preferences (email, push, in-app)

- [ ] **Alert Rules**:
    - Create custom alert conditions
    - Trigger alerts on: event count threshold, specific event types, severity levels
    - Rule actions: email, push notification, webhook
    - Rule management (enable/disable, edit, delete)

## MVP Features - Analytics & Monitoring

- [ ] **Event Analytics Dashboard**:
    - Event count by time period (charts)
    - Events by type/category (pie chart)
    - Events by severity distribution
    - Trend analysis (up/down indicators)

- [ ] **System Health Monitoring**:
    - Event ingestion rate metrics
    - System uptime tracking
    - Error rate monitoring
    - Performance metrics dashboard

- [ ] **Event Logs & Audit Trail**:
    - Complete event history
    - Audit log for user actions
    - Event modification history
    - Export audit logs

- [ ] **Custom Dashboards**:
    - Create personalized dashboard widgets
    - Drag-and-drop widget arrangement
    - Widget types: charts, counters, lists, timelines
    - Save/load dashboard layouts

## MVP Features - Integration & API

- [ ] **Event Ingestion API**:
    - REST API endpoint: POST /api/v1/events
    - API key authentication
    - Rate limiting per API key
    - Bulk event submission endpoint
