package asset.project.dto.response;

public record DashboardStatsRes(
    long totalAssets,
    long activeAssets,
    long inactiveAssets,
    long archivedAssets,
    long disposedAssets,
    long validationValid,
    long validationInvalid,
    long validationMissing,
    long validationPending
) {}