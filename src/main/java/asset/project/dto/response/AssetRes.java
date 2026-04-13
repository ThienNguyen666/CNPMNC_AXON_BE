package asset.project.dto.response;

import asset.project.enums.AssetCategory;
import asset.project.enums.AssetStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AssetRes(
    UUID id,
    String assetCode,
    String name,
    String description,
    AssetCategory category,
    AssetStatus status,
    BigDecimal purchasePrice,
    LocalDate purchaseDate,
    UUID departmentId,
    String departmentName,
    OffsetDateTime createdAt
) {}