package asset.project.service;

import asset.project.dto.request.AssetCreateReq;
import asset.project.dto.request.AssetTransferReq;
import asset.project.dto.request.AssetUpdateReq;
import asset.project.dto.response.AssetDetailRes;
import asset.project.dto.response.AssetRes;
import asset.project.dto.response.AssignmentRes;
import asset.project.dto.response.PageRes;
import asset.project.enums.AssetCategory;
import asset.project.enums.AssetStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AssetService {
    PageRes<AssetRes> getAll(UUID departmentId, AssetStatus status, AssetCategory category, String search, Pageable pageable);
    AssetDetailRes getById(UUID id);
    UUID create(AssetCreateReq req);
    void update(UUID id, AssetUpdateReq req);
    void archive(UUID id);
    void transfer(UUID id, AssetTransferReq req);
    void returnAsset(UUID id);
    List<AssignmentRes> getHistory(UUID id);
}