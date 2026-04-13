package asset.project.repository;

import asset.project.entity.AssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, UUID> {

    @Query("""
            SELECT aa FROM AssetAssignment aa
            LEFT JOIN FETCH aa.department
            LEFT JOIN FETCH aa.assignedBy
            WHERE aa.asset.id = :assetId
            ORDER BY aa.assignedAt DESC
            """)
    List<AssetAssignment> findByAssetIdOrderByAssignedAtDesc(@Param("assetId") UUID assetId);

    @Query("""
            SELECT aa FROM AssetAssignment aa
            WHERE aa.asset.id = :assetId AND aa.returnedAt IS NULL
            """)
    Optional<AssetAssignment> findOpenAssignment(@Param("assetId") UUID assetId);
}