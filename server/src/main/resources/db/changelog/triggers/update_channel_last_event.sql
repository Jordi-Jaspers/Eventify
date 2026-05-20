-- Trigger function to update channel.last_event_at on every event insert.
-- Also updates current_severity to reflect the latest event severity.
-- PERFORMANCE: This fires per row. If event volume exceeds 1000/sec or 50/sec per channel,
-- consider switching to a scheduled batch update approach. See migration XML for details.
CREATE OR REPLACE FUNCTION update_channel_last_event()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE channel
    SET last_event_at = NEW.timestamp,
        is_stale = false,
        current_severity = NEW.severity
    WHERE id = NEW.channel_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
