package asset.project.entity;

import asset.project.enums.ValidationSessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "validation_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ValidationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private Short year;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "validation_session_status", nullable = false)
    private ValidationSessionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by")
    private User initiatedBy;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}