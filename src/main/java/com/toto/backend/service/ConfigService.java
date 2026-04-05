package com.toto.backend.service;

import com.toto.backend.dto.object.FieldConfigDTO;
import com.toto.backend.entity.FieldConfig;
import com.toto.backend.repository.AddressConfigRepository;
import com.toto.backend.repository.FieldConfigRepository;
import com.toto.backend.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final FieldConfigRepository fieldConfigRepository;
    private final AddressConfigRepository addressConfigRepository;
    private final SystemConfigRepository systemConfigRepository;

    public Map<String, Object> getPublicSystemInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("IS_CONFIRM_FEATURE_ACTIVE",
                systemConfigRepository.findByConfigKey("IS_CONFIRM_FEATURE_ACTIVE")
                        .map(c -> "true".equalsIgnoreCase(c.getConfigValue())).orElse(false));
        info.put("MAX_UPLOAD_BYTES",
                systemConfigRepository.findByConfigKey("MAX_UPLOAD_BYTES")
                        .map(c -> c.getConfigValue()).orElse("5242880"));
        info.put("SESSION_TIMEOUT_MINUTES",
                systemConfigRepository.findByConfigKey("SESSION_TIMEOUT_MINUTES")
                        .map(c -> c.getConfigValue()).orElse("30"));
        return info;
    }

    public List<FieldConfigDTO> getFieldConfigs() {
        List<FieldConfig> configs = fieldConfigRepository.findAllByOrderByDisplayOrderAsc();
        List<FieldConfigDTO> result = new ArrayList<>();

        for (FieldConfig fc : configs) {
            List<String> dropdownOptions = new ArrayList<>();
            if (fc.getDropdownValues() != null && !fc.getDropdownValues().isBlank()) {
                dropdownOptions = Arrays.stream(fc.getDropdownValues().split("\\|"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }

            result.add(FieldConfigDTO.builder()
                    .fieldName(fc.getFieldName())
                    .fieldKey(fc.getFieldKey())
                    .isLocked(fc.getIsLocked() == 1)
                    .isOptionalUpload(Objects.equals(fc.getIsOptionalUpload(), 1))
                    .dropdownOptions(dropdownOptions)
                    .build());
        }
        return result;
    }

    public Map<String, List<String>> getDropdownConfig() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        List<FieldConfig> configs = fieldConfigRepository.findAllByOrderByDisplayOrderAsc();

        for (FieldConfig fc : configs) {
            if (fc.getDropdownValues() != null && !fc.getDropdownValues().isBlank()) {
                List<String> options = Arrays.stream(fc.getDropdownValues().split("\\|"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                result.put(fc.getFieldName(), options);
            }
        }
        return result;
    }

    public Map<String, List<String>> getAddressConfig() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        List<String> provinces = addressConfigRepository.findAllDistinctProvinces();

        for (String province : provinces) {
            List<String> districts = addressConfigRepository.findDistrictsByProvince(province);
            result.put(province, districts);
        }
        return result;
    }

}
