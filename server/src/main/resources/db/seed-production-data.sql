-- ============================================================================
-- EVENTIFY PRODUCTION SEED DATA
-- ============================================================================
-- This script seeds realistic data for the admin user (id=1) in production.
-- Run this AFTER the admin user has been created and migrations have run.
--
-- Prerequisites:
--   - Admin user exists with id=1
--   - All Liquibase migrations have been applied
--
-- Usage:
--   psql -h <host> -U <user> -d <database> -f seed-production-data.sql
-- ============================================================================

BEGIN;

-- ============================================================================
-- ORGANIZATIONS (5 total)
-- ============================================================================
INSERT INTO organization (name, slug, status, created_by, created_at)
VALUES
    ('Acme Corporation', 'acme-corp', 'ACTIVE', 1, NOW() - INTERVAL '45 days'),
    ('Beta Industries', 'beta-industries', 'ACTIVE', 1, NOW() - INTERVAL '40 days'),
    ('Gamma Solutions', 'gamma-solutions', 'ACTIVE', 1, NOW() - INTERVAL '35 days'),
    ('Delta Technologies', 'delta-tech', 'TRIAL', 1, NOW() - INTERVAL '30 days'),
    ('Epsilon Systems', 'epsilon-systems', 'ACTIVE', 1, NOW() - INTERVAL '25 days');

-- ============================================================================
-- ORGANIZATION MEMBERSHIPS (admin as OWNER of all 5)
-- ============================================================================
INSERT INTO organization_membership (user_id, organization_id, role, invited_by, joined_at)
VALUES
    (1, (SELECT id FROM organization WHERE slug = 'acme-corp'), 'OWNER', NULL, NOW() - INTERVAL '45 days'),
    (1, (SELECT id FROM organization WHERE slug = 'beta-industries'), 'OWNER', NULL, NOW() - INTERVAL '40 days'),
    (1, (SELECT id FROM organization WHERE slug = 'gamma-solutions'), 'OWNER', NULL, NOW() - INTERVAL '35 days'),
    (1, (SELECT id FROM organization WHERE slug = 'delta-tech'), 'OWNER', NULL, NOW() - INTERVAL '30 days'),
    (1, (SELECT id FROM organization WHERE slug = 'epsilon-systems'), 'OWNER', NULL, NOW() - INTERVAL '25 days');

-- ============================================================================
-- PERSONAL CHANNELS (10 channels for admin)
-- ============================================================================
INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
VALUES 
    ('Backend Errors', 'Production error tracking for backend services. Monitors API failures, database issues, and service exceptions.', 1, NULL, 'ACTIVE', NOW() - INTERVAL '50 days', NOW() - INTERVAL '2 hours'),
    ('Frontend Analytics', 'User behavior and performance metrics from the web application.', 1, NULL, 'ACTIVE', NOW() - INTERVAL '45 days', NOW() - INTERVAL '1 day'),
    ('Deployment Events', 'Tracks deployments, rollbacks, and infrastructure changes across all environments.', 1, NULL, 'ACTIVE', NOW() - INTERVAL '55 days', NOW() - INTERVAL '30 minutes'),
    ('Marketing Campaigns', 'Event tracking for seasonal marketing campaigns. Paused between campaigns.', 1, NULL, 'PAUSED', NOW() - INTERVAL '80 days', NOW() - INTERVAL '15 days'),
    ('Security Alerts', 'Critical security events including failed login attempts, suspicious activity, and access violations.', 1, NULL, 'ACTIVE', NOW() - INTERVAL '100 days', NOW() - INTERVAL '5 minutes'),
    ('Test Channel', NULL, 1, NULL, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    ('Mobile App Events', 'User interactions and errors from the iOS and Android mobile applications.', 1, NULL, 'ACTIVE', NOW() - INTERVAL '40 days', NOW() - INTERVAL '6 hours'),
    ('Payment Processing', 'Transaction events, payment failures, refunds, and billing notifications.', 1, NULL, 'ACTIVE', NOW() - INTERVAL '60 days', NOW() - INTERVAL '3 hours'),
    ('API Gateway Logs', 'Request/response logging for the API gateway with latency metrics.', 1, NULL, 'ACTIVE', NOW() - INTERVAL '35 days', NOW() - INTERVAL '45 minutes'),
    ('Scheduled Jobs', 'Cron job executions, batch processing results, and background task monitoring.', 1, NULL, 'ACTIVE', NOW() - INTERVAL '30 days', NOW() - INTERVAL '1 hour');

-- ============================================================================
-- ORGANIZATION CHANNELS (13 channels across 5 orgs)
-- ============================================================================

-- Acme Corp channels (3)
INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Acme Production Events', 'All production environment events for Acme Corp applications.', 1, 
       (SELECT id FROM organization WHERE slug = 'acme-corp'), 'ACTIVE',
       NOW() - INTERVAL '44 days', NOW() - INTERVAL '15 minutes';

INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Acme Staging Events', 'Pre-production testing and QA events.', 1, 
       (SELECT id FROM organization WHERE slug = 'acme-corp'), 'ACTIVE',
       NOW() - INTERVAL '42 days', NOW() - INTERVAL '2 hours';

INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Acme CI/CD Pipeline', 'Build, test, and deployment pipeline events.', 1, 
       (SELECT id FROM organization WHERE slug = 'acme-corp'), 'ACTIVE',
       NOW() - INTERVAL '40 days', NOW() - INTERVAL '10 minutes';

-- Beta Industries channels (3)
INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Beta Manufacturing Line', 'Real-time events from the manufacturing floor sensors.', 1, 
       (SELECT id FROM organization WHERE slug = 'beta-industries'), 'ACTIVE',
       NOW() - INTERVAL '39 days', NOW() - INTERVAL '5 minutes';

INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Beta Quality Control', 'Quality inspection results and defect tracking.', 1, 
       (SELECT id FROM organization WHERE slug = 'beta-industries'), 'PAUSED',
       NOW() - INTERVAL '38 days', NOW() - INTERVAL '20 days';

INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Beta Inventory System', 'Stock level changes, reorder alerts, and warehouse events.', 1, 
       (SELECT id FROM organization WHERE slug = 'beta-industries'), 'ACTIVE',
       NOW() - INTERVAL '35 days', NOW() - INTERVAL '1 hour';

-- Gamma Solutions channels (2)
INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Gamma Client Portal', 'Events from the client-facing web portal.', 1, 
       (SELECT id FROM organization WHERE slug = 'gamma-solutions'), 'ACTIVE',
       NOW() - INTERVAL '34 days', NOW() - INTERVAL '1 hour';

INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Gamma Support Tickets', 'Customer support ticket lifecycle events.', 1, 
       (SELECT id FROM organization WHERE slug = 'gamma-solutions'), 'ACTIVE',
       NOW() - INTERVAL '32 days', NOW() - INTERVAL '30 minutes';

-- Delta Technologies channels (2)
INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Delta RnD Lab', 'Research and development experiment tracking.', 1, 
       (SELECT id FROM organization WHERE slug = 'delta-tech'), 'ACTIVE',
       NOW() - INTERVAL '29 days', NOW() - INTERVAL '4 hours';

INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Delta Test Automation', 'Automated test suite execution results.', 1, 
       (SELECT id FROM organization WHERE slug = 'delta-tech'), 'ACTIVE',
       NOW() - INTERVAL '28 days', NOW() - INTERVAL '2 hours';

-- Epsilon Systems channels (2)
INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Epsilon Infrastructure', 'Cloud infrastructure monitoring and alerts.', 1, 
       (SELECT id FROM organization WHERE slug = 'epsilon-systems'), 'ACTIVE',
       NOW() - INTERVAL '24 days', NOW() - INTERVAL '20 minutes';

INSERT INTO channel (name, description, user_id, organization_id, status, created_at, updated_at)
SELECT 'Epsilon Database Ops', 'Database performance metrics and maintenance events.', 1, 
       (SELECT id FROM organization WHERE slug = 'epsilon-systems'), 'ACTIVE',
       NOW() - INTERVAL '22 days', NOW() - INTERVAL '45 minutes';

-- ============================================================================
-- PERSONAL WATCHLISTS (6 watchlists with channel groups)
-- ============================================================================

-- Watchlist 1: Production Monitoring
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'Production Monitoring',
    'Critical production services including backend errors, deployments, and security alerts.',
    1,
    NULL,
    jsonb_build_object(
        'groups', jsonb_build_array(
            jsonb_build_object(
                'id', '550e8400-e29b-41d4-a716-446655440001'::text,
                'name', 'Critical Services',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'Backend Errors' AND user_id = 1 AND organization_id IS NULL),
                    (SELECT id FROM channel WHERE name = 'Security Alerts' AND user_id = 1 AND organization_id IS NULL)
                )
            )
        ),
        'channelIds', jsonb_build_array(
            (SELECT id FROM channel WHERE name = 'Deployment Events' AND user_id = 1 AND organization_id IS NULL)
        )
    ),
    jsonb_build_object('timeRange', '24h', 'onlyCritical', true, 'sortBySeverity', true, 'groupedView', true),
    NOW() - INTERVAL '40 days',
    NOW() - INTERVAL '2 hours';

-- Watchlist 2: Frontend Performance
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'Frontend Performance',
    'Web application performance and user behavior tracking.',
    1,
    NULL,
    jsonb_build_object(
        'groups', jsonb_build_array(),
        'channelIds', jsonb_build_array(
            (SELECT id FROM channel WHERE name = 'Frontend Analytics' AND user_id = 1 AND organization_id IS NULL)
        )
    ),
    jsonb_build_object('timeRange', '24h', 'onlyCritical', false, 'sortBySeverity', true, 'groupedView', true),
    NOW() - INTERVAL '35 days',
    NOW() - INTERVAL '1 day';

-- Watchlist 3: All Channels Overview
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'All Channels Overview',
    'Complete view of all personal channels organized by category.',
    1,
    NULL,
    jsonb_build_object(
        'groups', jsonb_build_array(
            jsonb_build_object(
                'id', '550e8400-e29b-41d4-a716-446655440002'::text,
                'name', 'Application',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'Backend Errors' AND user_id = 1 AND organization_id IS NULL),
                    (SELECT id FROM channel WHERE name = 'Frontend Analytics' AND user_id = 1 AND organization_id IS NULL),
                    (SELECT id FROM channel WHERE name = 'Mobile App Events' AND user_id = 1 AND organization_id IS NULL)
                )
            ),
            jsonb_build_object(
                'id', '550e8400-e29b-41d4-a716-446655440003'::text,
                'name', 'Operations',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'Deployment Events' AND user_id = 1 AND organization_id IS NULL),
                    (SELECT id FROM channel WHERE name = 'Security Alerts' AND user_id = 1 AND organization_id IS NULL),
                    (SELECT id FROM channel WHERE name = 'Scheduled Jobs' AND user_id = 1 AND organization_id IS NULL)
                )
            ),
            jsonb_build_object(
                'id', '550e8400-e29b-41d4-a716-446655440004'::text,
                'name', 'Integrations',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'API Gateway Logs' AND user_id = 1 AND organization_id IS NULL),
                    (SELECT id FROM channel WHERE name = 'Payment Processing' AND user_id = 1 AND organization_id IS NULL)
                )
            )
        ),
        'channelIds', jsonb_build_array(
            (SELECT id FROM channel WHERE name = 'Marketing Campaigns' AND user_id = 1 AND organization_id IS NULL),
            (SELECT id FROM channel WHERE name = 'Test Channel' AND user_id = 1 AND organization_id IS NULL)
        )
    ),
    jsonb_build_object('timeRange', '7d', 'onlyCritical', false, 'sortBySeverity', false, 'groupedView', true),
    NOW() - INTERVAL '30 days',
    NOW() - INTERVAL '5 minutes';

-- Watchlist 4: Security Focus
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'Security Alerts Only',
    'Focused view for security monitoring. Shows only critical security events.',
    1,
    NULL,
    jsonb_build_object(
        'groups', jsonb_build_array(),
        'channelIds', jsonb_build_array(
            (SELECT id FROM channel WHERE name = 'Security Alerts' AND user_id = 1 AND organization_id IS NULL)
        )
    ),
    jsonb_build_object('timeRange', '30d', 'onlyCritical', true, 'sortBySeverity', true, 'groupedView', true),
    NOW() - INTERVAL '25 days',
    NOW() - INTERVAL '30 minutes';

-- Watchlist 5: Mobile and Payments
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'Mobile and Payments Dashboard',
    'Monitoring mobile app events alongside payment processing.',
    1,
    NULL,
    jsonb_build_object(
        'groups', jsonb_build_array(
            jsonb_build_object(
                'id', '550e8400-e29b-41d4-a716-446655440005'::text,
                'name', 'Mobile Commerce',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'Mobile App Events' AND user_id = 1 AND organization_id IS NULL),
                    (SELECT id FROM channel WHERE name = 'Payment Processing' AND user_id = 1 AND organization_id IS NULL)
                )
            )
        ),
        'channelIds', jsonb_build_array()
    ),
    jsonb_build_object('timeRange', '24h', 'onlyCritical', false, 'sortBySeverity', true, 'groupedView', true),
    NOW() - INTERVAL '20 days',
    NOW() - INTERVAL '4 hours';

-- Watchlist 6: DevOps Pipeline
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'DevOps Pipeline Monitor',
    'Deployment events, scheduled jobs, and API gateway monitoring.',
    1,
    NULL,
    jsonb_build_object(
        'groups', jsonb_build_array(
            jsonb_build_object(
                'id', '550e8400-e29b-41d4-a716-446655440006'::text,
                'name', 'DevOps',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'Deployment Events' AND user_id = 1 AND organization_id IS NULL),
                    (SELECT id FROM channel WHERE name = 'Scheduled Jobs' AND user_id = 1 AND organization_id IS NULL),
                    (SELECT id FROM channel WHERE name = 'API Gateway Logs' AND user_id = 1 AND organization_id IS NULL)
                )
            )
        ),
        'channelIds', jsonb_build_array()
    ),
    jsonb_build_object('timeRange', '24h', 'onlyCritical', false, 'sortBySeverity', true, 'groupedView', true),
    NOW() - INTERVAL '15 days',
    NOW() - INTERVAL '1 hour';

-- ============================================================================
-- ORGANIZATION WATCHLISTS (6 watchlists for orgs)
-- ============================================================================

-- Acme Corp: Full Stack Monitoring
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'Acme Full Stack',
    'Complete monitoring of Acme Corp production and staging environments.',
    1,
    (SELECT id FROM organization WHERE slug = 'acme-corp'),
    jsonb_build_object(
        'groups', jsonb_build_array(
            jsonb_build_object(
                'id', '650e8400-e29b-41d4-a716-446655440001'::text,
                'name', 'Environments',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'Acme Production Events' AND organization_id = (SELECT id FROM organization WHERE slug = 'acme-corp')),
                    (SELECT id FROM channel WHERE name = 'Acme Staging Events' AND organization_id = (SELECT id FROM organization WHERE slug = 'acme-corp'))
                )
            )
        ),
        'channelIds', jsonb_build_array(
            (SELECT id FROM channel WHERE name = 'Acme CI/CD Pipeline' AND organization_id = (SELECT id FROM organization WHERE slug = 'acme-corp'))
        )
    ),
    jsonb_build_object('timeRange', '24h', 'onlyCritical', false, 'sortBySeverity', true, 'groupedView', true),
    NOW() - INTERVAL '40 days',
    NOW() - INTERVAL '1 hour';

-- Acme Corp: Production Only
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'Acme Production Critical',
    'Production-only view for on-call monitoring.',
    1,
    (SELECT id FROM organization WHERE slug = 'acme-corp'),
    jsonb_build_object(
        'groups', jsonb_build_array(),
        'channelIds', jsonb_build_array(
            (SELECT id FROM channel WHERE name = 'Acme Production Events' AND organization_id = (SELECT id FROM organization WHERE slug = 'acme-corp'))
        )
    ),
    jsonb_build_object('timeRange', '24h', 'onlyCritical', true, 'sortBySeverity', true, 'groupedView', true),
    NOW() - INTERVAL '35 days',
    NOW() - INTERVAL '30 minutes';

-- Beta Industries: Manufacturing Dashboard
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'Beta Manufacturing Dashboard',
    'Real-time manufacturing and inventory monitoring.',
    1,
    (SELECT id FROM organization WHERE slug = 'beta-industries'),
    jsonb_build_object(
        'groups', jsonb_build_array(
            jsonb_build_object(
                'id', '650e8400-e29b-41d4-a716-446655440002'::text,
                'name', 'Manufacturing Operations',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'Beta Manufacturing Line' AND organization_id = (SELECT id FROM organization WHERE slug = 'beta-industries')),
                    (SELECT id FROM channel WHERE name = 'Beta Quality Control' AND organization_id = (SELECT id FROM organization WHERE slug = 'beta-industries')),
                    (SELECT id FROM channel WHERE name = 'Beta Inventory System' AND organization_id = (SELECT id FROM organization WHERE slug = 'beta-industries'))
                )
            )
        ),
        'channelIds', jsonb_build_array()
    ),
    jsonb_build_object('timeRange', '24h', 'onlyCritical', false, 'sortBySeverity', true, 'groupedView', true),
    NOW() - INTERVAL '30 days',
    NOW() - INTERVAL '15 minutes';

-- Gamma Solutions: Client Operations
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'Gamma Client Operations',
    'Client portal and support ticket monitoring.',
    1,
    (SELECT id FROM organization WHERE slug = 'gamma-solutions'),
    jsonb_build_object(
        'groups', jsonb_build_array(
            jsonb_build_object(
                'id', '650e8400-e29b-41d4-a716-446655440003'::text,
                'name', 'Client Experience',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'Gamma Client Portal' AND organization_id = (SELECT id FROM organization WHERE slug = 'gamma-solutions')),
                    (SELECT id FROM channel WHERE name = 'Gamma Support Tickets' AND organization_id = (SELECT id FROM organization WHERE slug = 'gamma-solutions'))
                )
            )
        ),
        'channelIds', jsonb_build_array()
    ),
    jsonb_build_object('timeRange', '24h', 'onlyCritical', false, 'sortBySeverity', true, 'groupedView', true),
    NOW() - INTERVAL '28 days',
    NOW() - INTERVAL '45 minutes';

-- Delta Technologies: RnD Monitoring
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'Delta RnD Monitor',
    'Research lab and test automation tracking.',
    1,
    (SELECT id FROM organization WHERE slug = 'delta-tech'),
    jsonb_build_object(
        'groups', jsonb_build_array(
            jsonb_build_object(
                'id', '650e8400-e29b-41d4-a716-446655440004'::text,
                'name', 'Research and Testing',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'Delta RnD Lab' AND organization_id = (SELECT id FROM organization WHERE slug = 'delta-tech')),
                    (SELECT id FROM channel WHERE name = 'Delta Test Automation' AND organization_id = (SELECT id FROM organization WHERE slug = 'delta-tech'))
                )
            )
        ),
        'channelIds', jsonb_build_array()
    ),
    jsonb_build_object('timeRange', '7d', 'onlyCritical', false, 'sortBySeverity', false, 'groupedView', true),
    NOW() - INTERVAL '25 days',
    NOW() - INTERVAL '3 hours';

-- Epsilon Systems: Infrastructure
INSERT INTO watchlist (name, description, user_id, organization_id, configuration, filters, created_at, updated_at)
SELECT
    'Epsilon Infrastructure Dashboard',
    'Cloud infrastructure and database operations monitoring.',
    1,
    (SELECT id FROM organization WHERE slug = 'epsilon-systems'),
    jsonb_build_object(
        'groups', jsonb_build_array(
            jsonb_build_object(
                'id', '650e8400-e29b-41d4-a716-446655440005'::text,
                'name', 'Infrastructure',
                'channelIds', jsonb_build_array(
                    (SELECT id FROM channel WHERE name = 'Epsilon Infrastructure' AND organization_id = (SELECT id FROM organization WHERE slug = 'epsilon-systems')),
                    (SELECT id FROM channel WHERE name = 'Epsilon Database Ops' AND organization_id = (SELECT id FROM organization WHERE slug = 'epsilon-systems'))
                )
            )
        ),
        'channelIds', jsonb_build_array()
    ),
    jsonb_build_object('timeRange', '24h', 'onlyCritical', false, 'sortBySeverity', true, 'groupedView', true),
    NOW() - INTERVAL '20 days',
    NOW() - INTERVAL '1 hour';

-- ============================================================================
-- USER EVENT QUOTA (45% usage)
-- ============================================================================
INSERT INTO user_event_quota (user_id, event_count, period_start, updated_at)
VALUES (
    1,
    450,
    DATE_TRUNC('month', NOW()),
    NOW() - INTERVAL '2 hours'
);

-- ============================================================================
-- EVENTS - 7 DAYS FOR PERSONAL CHANNELS
-- ============================================================================
-- Pre-generate severity to ensure message matches
WITH time_slots AS (
    SELECT generate_series(
        (NOW() - INTERVAL '7 days')::timestamptz,
        NOW()::timestamptz,
        INTERVAL '5 minutes'
    ) AS slot
),
random_slots AS (
    SELECT slot FROM time_slots WHERE random() < 0.5
),
event_data AS (
    SELECT
        c.id AS channel_id,
        c.name AS channel_name,
        rs.slot,
        CASE 
            WHEN random() < 0.70 THEN 'OK'
            WHEN random() < 0.90 THEN 'WARNING'
            ELSE 'CRITICAL'
        END AS severity
    FROM
        channel c
        CROSS JOIN random_slots rs
    WHERE
        c.status = 'ACTIVE'
        AND c.user_id = 1
        AND c.organization_id IS NULL
)
INSERT INTO event (channel_id, severity, title, message, metadata, timestamp)
SELECT
    ed.channel_id,
    ed.severity,
    CASE ed.channel_name
        WHEN 'Backend Errors' THEN 
            (ARRAY['API Response', 'Database Query', 'Service Call', 'Cache Hit', 'Request Processed'])[FLOOR(random() * 5 + 1)::int]
        WHEN 'Frontend Analytics' THEN 
            (ARRAY['Page View', 'User Click', 'Form Submit', 'Navigation', 'Session Start'])[FLOOR(random() * 5 + 1)::int]
        WHEN 'Deployment Events' THEN 
            (ARRAY['Build Started', 'Tests Passed', 'Deploy Complete', 'Rollback', 'Health Check'])[FLOOR(random() * 5 + 1)::int]
        WHEN 'Security Alerts' THEN 
            (ARRAY['Login Attempt', 'Token Refresh', 'Access Denied', 'Rate Limited', 'Session Valid'])[FLOOR(random() * 5 + 1)::int]
        WHEN 'Mobile App Events' THEN
            (ARRAY['App Launch', 'Screen View', 'User Action', 'Push Received', 'Crash Report'])[FLOOR(random() * 5 + 1)::int]
        WHEN 'Payment Processing' THEN
            (ARRAY['Payment Init', 'Payment Success', 'Payment Failed', 'Refund Issued', 'Subscription Renewed'])[FLOOR(random() * 5 + 1)::int]
        WHEN 'API Gateway Logs' THEN
            (ARRAY['Request Received', 'Response Sent', 'Rate Limit Hit', 'Auth Failed', 'Route Matched'])[FLOOR(random() * 5 + 1)::int]
        WHEN 'Scheduled Jobs' THEN
            (ARRAY['Job Started', 'Job Completed', 'Job Failed', 'Job Skipped', 'Job Retrying'])[FLOOR(random() * 5 + 1)::int]
        ELSE 
            (ARRAY['Event Received', 'Status Update', 'Metric Report', 'Heartbeat', 'Notification'])[FLOOR(random() * 5 + 1)::int]
    END AS title,
    CASE ed.severity
        WHEN 'CRITICAL' THEN 'Critical issue detected - immediate attention required for ' || ed.channel_name
        WHEN 'WARNING' THEN 'Warning threshold exceeded in ' || ed.channel_name || ' - monitoring closely'
        ELSE 'Normal operation for ' || ed.channel_name || ' - all systems nominal'
    END AS message,
    jsonb_build_object(
        'source', (ARRAY['api-server', 'worker-1', 'worker-2', 'scheduler', 'gateway'])[FLOOR(random() * 5 + 1)::int],
        'channel_name', ed.channel_name,
        'environment', (ARRAY['production', 'staging', 'development'])[FLOOR(random() * 3 + 1)::int],
        'region', (ARRAY['us-east-1', 'eu-west-1', 'ap-southeast-1'])[FLOOR(random() * 3 + 1)::int],
        'cpu_usage', ROUND((random() * 80 + 10)::numeric, 1),
        'memory_mb', (random() * 4096)::int,
        'response_time_ms', (random() * 500)::int,
        'request_count', (random() * 1000)::int
    ) AS metadata,
    ed.slot + (random() * INTERVAL '4 minutes' - INTERVAL '2 minutes') AS timestamp
FROM event_data ed
ON CONFLICT DO NOTHING;

-- ============================================================================
-- EVENTS - INCIDENT BURSTS (simulated incidents)
-- ============================================================================
WITH incident_times AS (
    SELECT unnest(ARRAY[
        NOW() - INTERVAL '6 days' + INTERVAL '14 hours',
        NOW() - INTERVAL '3 days' + INTERVAL '9 hours',
        NOW() - INTERVAL '1 day' + INTERVAL '16 hours'
    ]) AS incident_start
),
burst_events AS (
    SELECT 
        incident_start + (n || ' minutes')::interval AS event_time,
        CASE 
            WHEN n < 5 THEN 'CRITICAL'
            WHEN n < 15 THEN 'WARNING'
            ELSE 'OK'
        END AS severity
    FROM incident_times, generate_series(0, 30) AS n
)
INSERT INTO event (channel_id, severity, title, message, metadata, timestamp)
SELECT 
    (SELECT id FROM channel WHERE name = 'Backend Errors' AND user_id = 1 AND organization_id IS NULL),
    be.severity,
    CASE be.severity
        WHEN 'CRITICAL' THEN 'Service Outage Detected'
        WHEN 'WARNING' THEN 'Service Degradation'
        ELSE 'Service Recovered'
    END,
    CASE be.severity
        WHEN 'CRITICAL' THEN 'Multiple services reporting failures. Incident response initiated.'
        WHEN 'WARNING' THEN 'Elevated error rates detected. Monitoring situation.'
        ELSE 'Services have recovered. Incident resolved.'
    END,
    jsonb_build_object(
        'incident_id', 'INC-' || FLOOR(random() * 10000)::int,
        'affected_services', ARRAY['api', 'database', 'cache'],
        'error_rate', CASE be.severity WHEN 'CRITICAL' THEN 45.2 WHEN 'WARNING' THEN 12.5 ELSE 0.3 END,
        'source', 'incident-detector'
    ),
    be.event_time
FROM burst_events be
ON CONFLICT DO NOTHING;

-- ============================================================================
-- EVENTS - 7 DAYS FOR ORGANIZATION CHANNELS  
-- ============================================================================
WITH time_slots AS (
    SELECT generate_series(
        (NOW() - INTERVAL '7 days')::timestamptz,
        NOW()::timestamptz,
        INTERVAL '10 minutes'
    ) AS slot
),
random_slots AS (
    SELECT slot FROM time_slots WHERE random() < 0.4
)
INSERT INTO event (channel_id, severity, title, message, metadata, timestamp)
SELECT
    c.id AS channel_id,
    CASE 
        WHEN random() < 0.75 THEN 'OK'
        WHEN random() < 0.92 THEN 'WARNING'
        ELSE 'CRITICAL'
    END AS severity,
    CASE 
        WHEN c.name LIKE 'Acme%' THEN (ARRAY['Deploy Success', 'Build Complete', 'Test Pass', 'Config Update', 'Scale Event'])[FLOOR(random() * 5 + 1)::int]
        WHEN c.name LIKE 'Beta%' THEN (ARRAY['Sensor Reading', 'Quality Check', 'Inventory Update', 'Machine Status', 'Alert'])[FLOOR(random() * 5 + 1)::int]
        WHEN c.name LIKE 'Gamma%' THEN (ARRAY['User Login', 'Ticket Created', 'Ticket Resolved', 'Portal Error', 'Session Expired'])[FLOOR(random() * 5 + 1)::int]
        WHEN c.name LIKE 'Delta%' THEN (ARRAY['Experiment Started', 'Test Run', 'Results Published', 'Pipeline Complete', 'Analysis Done'])[FLOOR(random() * 5 + 1)::int]
        WHEN c.name LIKE 'Epsilon%' THEN (ARRAY['CPU Alert', 'Memory Warning', 'Disk Usage', 'Network Latency', 'Service Health'])[FLOOR(random() * 5 + 1)::int]
        ELSE (ARRAY['Event', 'Update', 'Notification', 'Alert', 'Status'])[FLOOR(random() * 5 + 1)::int]
    END AS title,
    'Automated event from ' || c.name || ' - routine monitoring',
    jsonb_build_object(
        'org_channel', c.name,
        'automated', true,
        'priority', (ARRAY['low', 'medium', 'high'])[FLOOR(random() * 3 + 1)::int],
        'processing_time_ms', (random() * 200)::int
    ) AS metadata,
    rs.slot + (random() * INTERVAL '8 minutes' - INTERVAL '4 minutes') AS timestamp
FROM
    channel c
    CROSS JOIN random_slots rs
WHERE
    c.status = 'ACTIVE'
    AND c.organization_id IS NOT NULL
ON CONFLICT DO NOTHING;

COMMIT;

-- ============================================================================
-- SUMMARY
-- ============================================================================
-- Data created:
--   - 5 organizations (Acme, Beta, Gamma, Delta, Epsilon)
--   - 5 organization memberships (admin as OWNER)
--   - 10 personal channels
--   - 13 organization channels (total: 23 channels)
--   - 6 personal watchlists with groups
--   - 6 organization watchlists with groups
--   - 1 user quota record (45% usage)
--   - ~10,000+ events across 7 days
-- ============================================================================
