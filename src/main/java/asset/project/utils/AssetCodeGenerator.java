package asset.project.utils;

import asset.project.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
@RequiredArgsConstructor
public class AssetCodeGenerator {

    private final AssetRepository assetRepository;

    /**
     * Generates codes like IT-2024-001 based on department code and current year.
     */
    public String generate(String departmentCode) {
        String prefix = departmentCode.toUpperCase() + "-" + Year.now().getValue() + "-";
        long count = assetRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }
}