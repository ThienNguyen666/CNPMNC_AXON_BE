package asset.project.service.impl;

import asset.project.dto.request.OpenValidationSessionReq;
import asset.project.dto.request.ValidationStatusReq;
import asset.project.dto.response.ValidationRecordRes;
import asset.project.dto.response.ValidationSessionRes;
import asset.project.entity.Asset;
import asset.project.entity.User;
import asset.project.entity.ValidationRecord;
import asset.project.entity.ValidationSession;
import asset.project.enums.ValidationRecordStatus;
import asset.project.enums.ValidationSessionStatus;
import asset.project.exception.BusinessException;
import asset.project.exception.ResourceNotFoundException;
import asset.project.repository.AssetRepository;
import asset.project.repository.ValidationRecordRepository;
import asset.project.repository.ValidationSessionRepository;
import asset.project.service.AuditLogService;
import asset.project.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

    @Mock
    private ValidationSessionRepository sessionRepository;
    @Mock
    private ValidationRecordRepository recordRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ValidationServiceImpl validationService;

    private User mockUser;
    private ValidationSession mockSession;
    private UUID sessionId;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("Test Auditor")
                .email("auditor@example.com")
                .build();

        sessionId = UUID.randomUUID();
        mockSession = ValidationSession.builder()
                .id(sessionId)
                .year((short) 2024)
                .status(ValidationSessionStatus.in_progress)
                .initiatedBy(mockUser)
                .startedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void getAllSessions_ShouldReturnList() {
        when(sessionRepository.findAllByOrderByYearDesc()).thenReturn(List.of(mockSession));
        
        List<ValidationSessionRes> result = validationService.getAllSessions();
        
        assertFalse(result.isEmpty());
        assertEquals(sessionId, result.get(0).id());
        verify(sessionRepository).findAllByOrderByYearDesc();
    }

    @Test
    void getSessionById_ShouldReturnSession() {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(mockSession));
        
        ValidationSessionRes result = validationService.getSessionById(sessionId);
        
        assertNotNull(result);
        assertEquals(sessionId, result.id());
    }

    @Test
    void getSessionById_ShouldThrowException_WhenNotFound() {
        when(sessionRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> validationService.getSessionById(UUID.randomUUID()));
    }

    @Test
    void openSession_ShouldCreateSessionAndRecords() {
        OpenValidationSessionReq req = new OpenValidationSessionReq((short) 2025, "New session");
        
        when(sessionRepository.existsByYear(req.year())).thenReturn(false);
        when(sessionRepository.findByStatus(ValidationSessionStatus.in_progress)).thenReturn(Optional.empty());
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(sessionRepository.save(any())).thenAnswer(invocation -> {
            ValidationSession s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        
        Asset activeAsset = Asset.builder().id(UUID.randomUUID()).assetCode("AST-001").build();
        when(assetRepository.findAllActive()).thenReturn(List.of(activeAsset));

        ValidationSessionRes result = validationService.openSession(req);

        assertNotNull(result);
        assertEquals(req.year(), result.year());
        verify(recordRepository).saveAll(anyList());
        verify(auditLogService).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void openSession_ShouldThrow_WhenYearExists() {
        OpenValidationSessionReq req = new OpenValidationSessionReq((short) 2024, "Existing year");
        when(sessionRepository.existsByYear(req.year())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> validationService.openSession(req));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void openSession_ShouldThrow_WhenAnotherInProgress() {
        OpenValidationSessionReq req = new OpenValidationSessionReq((short) 2025, "Another progress");
        when(sessionRepository.existsByYear(req.year())).thenReturn(false);
        when(sessionRepository.findByStatus(ValidationSessionStatus.in_progress)).thenReturn(Optional.of(mockSession));

        BusinessException ex = assertThrows(BusinessException.class, () -> validationService.openSession(req));
        assertEquals("Another session is already in progress", ex.getMessage());
    }

    @Test
    void closeSession_ShouldSuccess() {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(mockSession));
        
        validationService.closeSession(sessionId);
        
        assertEquals(ValidationSessionStatus.closed, mockSession.getStatus());
        assertNotNull(mockSession.getClosedAt());
        verify(sessionRepository).save(mockSession);
    }

    @Test
    void closeSession_ShouldThrow_WhenAlreadyClosed() {
        mockSession.setStatus(ValidationSessionStatus.closed);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(mockSession));
        
        assertThrows(BusinessException.class, () -> validationService.closeSession(sessionId));
    }

    @Test
    void getRecords_ShouldReturnList() {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(mockSession));
        
        Asset asset = Asset.builder().id(UUID.randomUUID()).assetCode("AST-1").name("Asset 1").build();
        ValidationRecord record = ValidationRecord.builder()
                .id(UUID.randomUUID())
                .session(mockSession)
                .asset(asset)
                .status(ValidationRecordStatus.pending)
                .build();
        
        when(recordRepository.findBySessionFiltered(eq(sessionId), any(), any()))
                .thenReturn(List.of(record));

        List<ValidationRecordRes> result = validationService.getRecords(sessionId, null, null);
        
        assertFalse(result.isEmpty());
        assertEquals(record.getId(), result.get(0).id());
    }

    @Test
    void submitValidationStatus_ShouldSuccess() {
        UUID assetId = UUID.randomUUID();
        Asset asset = Asset.builder().id(assetId).assetCode("AST-1").build();
        ValidationRecord record = ValidationRecord.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .status(ValidationRecordStatus.pending)
                .build();
        
        ValidationStatusReq req = new ValidationStatusReq(ValidationRecordStatus.valid, "Verified");
        
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(recordRepository.findActiveRecordForAsset(assetId)).thenReturn(Optional.of(record));
        
        validationService.submitValidationStatus(assetId, req);
        
        assertEquals(ValidationRecordStatus.valid, record.getStatus());
        assertEquals("Verified", record.getNotes());
        assertEquals(mockUser, record.getValidatedBy());
        assertNotNull(record.getValidatedAt());
        verify(recordRepository).save(record);
        verify(auditLogService).log(any(), any(), eq(asset), any(), any(), any(), any());
    }

    @Test
    void submitValidationStatus_ShouldThrow_WhenNoActiveRecord() {
        UUID assetId = UUID.randomUUID();
        when(recordRepository.findActiveRecordForAsset(assetId)).thenReturn(Optional.empty());
        
        ValidationStatusReq req = new ValidationStatusReq(ValidationRecordStatus.valid, "Verified");
        
        assertThrows(BusinessException.class, () -> validationService.submitValidationStatus(assetId, req));
    }
}
