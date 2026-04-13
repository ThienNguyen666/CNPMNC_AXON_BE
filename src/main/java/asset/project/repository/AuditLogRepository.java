package asset.project.repository;

import asset.project.entity.AuditLog;
import asset.project.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("""
            SELECT al FROM AuditLog al
            LEFT JOIN FETCH al.performedBy
            LEFT JOIN FETCH al.asset
            WHERE (:action IS NULL OR al.action = :action)
              AND (:assetId IS NULL OR al.asset.id = :assetId)
              AND (:performedById IS NULL OR al.performedBy.id = :performedById)
              AND (:from IS NULL OR al.createdAt >= :from)
              AND (:to IS NULL OR al.createdAt <= :to)
            ORDER BY al.createdAt DESC
            """)
    Page<AuditLog> findAllFiltered(
            @Param("action") AuditAction action,
            @Param("assetId") UUID assetId,
            @Param("performedById") UUID performedById,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable);

    List<AuditLog> findByAssetIdOrderByCreatedAtDesc(UUID assetId);
}