package asset.project.entity;

import asset.project.enums.ValidationRecordStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "validation_records",
       uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "asset_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ValidationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ValidationSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "validation_record_status", nullable = false)
    private ValidationRecordStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by")
    private User validatedBy;

    @Column(name = "validated_at")
    private OffsetDateTime validatedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}