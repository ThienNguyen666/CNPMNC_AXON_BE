package asset.project.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record DepartmentStatsRes(
    UUID departmentId,
    String departmentName,
    long assetCount,
    BigDecimal totalValue
) {}