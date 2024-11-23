package org.jordijaspers.eventify.api.source.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

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
}
