CREATE FUNCTION update_membership_count()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE organization
        SET member_count = member_count + 1
        WHERE id = NEW.organization_id;

    ELSIF TG_OP = 'DELETE' THEN
        UPDATE organization
        SET member_count = member_count - 1
        WHERE id = OLD.organization_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;
