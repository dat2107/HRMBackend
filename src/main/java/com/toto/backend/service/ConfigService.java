package com.toto.backend.service;

import com.toto.backend.dto.object.FieldConfigDTO;

import java.util.List;
import java.util.Map;

public interface ConfigService {
    Map<String, Object> getPublicSystemInfo();
    List<FieldConfigDTO> getFieldConfigs();
    Map<String, List<String>> getDropdownConfig();
    Map<String, List<String>> getAddressConfig();
}
