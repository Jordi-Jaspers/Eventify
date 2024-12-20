package org.jordijaspers.eventify.api.check.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.jordijaspers.eventify.api.source.model.Source;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "\"check\"")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Check implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @CreationTimestamp
    @Column(
        name = "created",
        updatable = false
    )
    private LocalDateTime created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "source_id",
        nullable = false
    )
    private Source source;

    /**
     * A constructor to create a persistent Check object.
     *
     * @param id The id of the Check object.
     */
    public Check(final Long id) {
        this.id = id;
    }

}
