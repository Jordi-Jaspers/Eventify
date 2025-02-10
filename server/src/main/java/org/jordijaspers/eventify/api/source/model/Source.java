package org.jordijaspers.eventify.api.source.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.source.model.request.SourceRequest;

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

    @OneToMany(
        mappedBy = "source",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<Check> checks = new ArrayList<>();

    /**
     * A constructor to create a persistent Source object.
     *
     * @param request The request to create a Source object.
     */
    public Source(final SourceRequest request) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.apiKey = new ApiKey();
    }

    /**
     * Sets the API key enabled or disabled.
     *
     * @param enabled true if the API key is enabled, false otherwise.
     */
    public void setApiKeyEnabled(boolean enabled) {
        this.apiKey.setEnabled(enabled);
    }
}
