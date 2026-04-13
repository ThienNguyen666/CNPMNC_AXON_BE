package asset.project.repository;

import asset.project.entity.Asset;
import asset.project.enums.AssetCategory;
import asset.project.enums.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

    boolean existsByAssetCode(String assetCode);

    @Query("""
    SELECT a FROM Asset a
    LEFT JOIN a.currentDepartment d
    WHERE (:departmentId IS NULL OR d.id = :departmentId)
      AND (:status IS NULL OR a.status = :status)
      AND (:category IS NULL OR a.category = :category)
      AND (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(a.assetCode) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<Asset> findAllFiltered(
            @Param("departmentId") UUID departmentId,
            @Param("status") AssetStatus status,
            @Param("category") AssetCategory category,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.status = 'active' ORDER BY a.assetCode")
    List<Asset> findAllActive();

    long countByStatus(AssetStatus status);

    @Query("""
            SELECT a.currentDepartment.id, a.currentDepartment.name,
                   COUNT(a), SUM(a.purchasePrice)
            FROM Asset a
            WHERE a.currentDepartment IS NOT NULL
            GROUP BY a.currentDepartment.id, a.currentDepartment.name
            """)
    List<Object[]> countAndSumByDepartment();
}