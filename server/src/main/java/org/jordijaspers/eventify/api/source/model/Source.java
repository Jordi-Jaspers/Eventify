package org.jordijaspers.eventify.api.source.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.jordijaspers.eventify.api.source.model.request.CreateSourceRequest;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "source")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Source implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(
        name = "created",
        updatable = false
    )
    private LocalDateTime created;

    @OneToOne(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private ApiKey apiKey;

    /**
     * A constructor to create a persistent Source object.
     *
     * @param request The request to create a Source object.
     */
    public Source(final CreateSourceRequest request) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.apiKey = new ApiKey();
    }
}
