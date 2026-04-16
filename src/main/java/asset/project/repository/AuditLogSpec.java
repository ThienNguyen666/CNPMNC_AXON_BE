package asset.project.repository;

import asset.project.entity.AuditLog;
import asset.project.enums.AuditAction;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AuditLogSpec {

    private AuditLogSpec() {}

    public static Specification<AuditLog> withAction(AuditAction action) {
        return (root, query, cb) ->
                action == null ? null : cb.equal(root.get("action"), action);
    }

    public static Specification<AuditLog> withAssetId(UUID assetId) {
        return (root, query, cb) -> {
            if (assetId == null) return null;
            var join = root.join("asset", JoinType.LEFT);
            return cb.equal(join.get("id"), assetId);
        };
    }

    public static Specification<AuditLog> withPerformedById(UUID performedById) {
        return (root, query, cb) -> {
            if (performedById == null) return null;
            var join = root.join("performedBy", JoinType.LEFT);
            return cb.equal(join.get("id"), performedById);
        };
    }

    public static Specification<AuditLog> fromDate(OffsetDateTime from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<AuditLog> toDate(OffsetDateTime to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<AuditLog> build(
            AuditAction action,
            UUID assetId,
            UUID performedById,
            OffsetDateTime from,
            OffsetDateTime to) {

        return Specification.where(withAction(action))
                .and(withAssetId(assetId))
                .and(withPerformedById(performedById))
                .and(fromDate(from))
                .and(toDate(to));
    }
}