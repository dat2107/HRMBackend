package com.toto.backend.controller;

import com.toto.backend.common.BaseResponse;
import com.toto.backend.common.BaseResponseFactory;
import com.toto.backend.dto.object.FieldConfigDTO;
import com.toto.backend.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;
    private final BaseResponseFactory responseFactory;

    @GetMapping("/system")
    public BaseResponse<Map<String, Object>> getSystemInfo() {
        return responseFactory.success("success.default", configService.getPublicSystemInfo());
    }

    @GetMapping("/fields")
    public BaseResponse<List<FieldConfigDTO>> getFieldConfigs() {
        return responseFactory.success("success.default", configService.getFieldConfigs());
    }

    @GetMapping("/dropdowns")
    public BaseResponse<Map<String, List<String>>> getDropdowns() {
        return responseFactory.success("success.default", configService.getDropdownConfig());
    }

    @GetMapping("/address")
    public BaseResponse<Map<String, List<String>>> getAddress() {
        return responseFactory.success("success.default", configService.getAddressConfig());
    }
}
