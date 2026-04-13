package asset.project.repository;

import asset.project.entity.ValidationRecord;
import asset.project.enums.ValidationRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValidationRecordRepository extends JpaRepository<ValidationRecord, UUID> {

    List<ValidationRecord> findBySessionId(UUID sessionId);

    @Query("""
            SELECT vr FROM ValidationRecord vr
            LEFT JOIN FETCH vr.asset a
            LEFT JOIN FETCH a.currentDepartment
            WHERE vr.session.id = :sessionId
              AND (:status IS NULL OR vr.status = :status)
              AND (:departmentId IS NULL OR a.currentDepartment.id = :departmentId)
            """)
    List<ValidationRecord> findBySessionFiltered(
            @Param("sessionId") UUID sessionId,
            @Param("status") ValidationRecordStatus status,
            @Param("departmentId") UUID departmentId);

    @Query("""
            SELECT vr FROM ValidationRecord vr
            WHERE vr.session.status = 'in_progress'
              AND vr.asset.id = :assetId
            """)
    Optional<ValidationRecord> findActiveRecordForAsset(@Param("assetId") UUID assetId);

    long countBySessionIdAndStatus(UUID sessionId, ValidationRecordStatus status);
}