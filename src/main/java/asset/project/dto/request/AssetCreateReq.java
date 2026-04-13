package asset.project.dto.request;

import asset.project.enums.AssetCategory;
import asset.project.enums.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AssetCreateReq(
    @NotBlank(message = "Asset name is required")
    String name,

    String description,

    @NotNull(message = "Category is required")
    AssetCategory category,

    @NotNull(message = "Status is required")
    AssetStatus status,

    BigDecimal purchasePrice,
    LocalDate purchaseDate,
    UUID departmentId
) {}