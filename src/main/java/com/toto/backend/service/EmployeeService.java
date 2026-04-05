package com.toto.backend.service;

import com.toto.backend.dto.request.UpdateRequestDto;
import com.toto.backend.dto.response.EmployeeProfileResponse;

public interface EmployeeService {
    EmployeeProfileResponse getProfile(String employeeId);
    void submitUpdateRequest(String employeeId, UpdateRequestDto dto);
    void submitConfirmation(String employeeId);
}
