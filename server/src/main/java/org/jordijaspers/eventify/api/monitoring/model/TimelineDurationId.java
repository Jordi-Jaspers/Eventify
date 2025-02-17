package org.jordijaspers.eventify.api.monitoring.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

/**
 * Compound id class for DbTimelineDuration.
 */
@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public final class TimelineDurationId implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Column(name = "check_id")
    private Long checkId;

    @Column(name = "start_time")
    private ZonedDateTime startTime;

    @Override
    public int hashCode() {
        return Objects.hash(this.startTime, this.checkId);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        final TimelineDurationId dbTimelineDurationId = (TimelineDurationId) obj;
        return dbTimelineDurationId.checkId.equals(this.checkId)
            && dbTimelineDurationId.startTime.equals(this.startTime);

    }
}
