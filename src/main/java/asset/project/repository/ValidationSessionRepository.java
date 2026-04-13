package asset.project.repository;

import asset.project.entity.ValidationSession;
import asset.project.enums.ValidationSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValidationSessionRepository extends JpaRepository<ValidationSession, UUID> {

    boolean existsByYear(Short year);

    Optional<ValidationSession> findByStatus(ValidationSessionStatus status);

    List<ValidationSession> findAllByOrderByYearDesc();
}