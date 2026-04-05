package com.toto.backend.service.impl;

import com.toto.backend.entity.Employee;
import com.toto.backend.entity.UpdateRequest;
import com.toto.backend.enums.Status;
import com.toto.backend.exception.AppException;
import com.toto.backend.exception.ErrorCode;
import com.toto.backend.repository.EmployeeRepository;
import com.toto.backend.repository.UpdateRequestRepository;
import com.toto.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UpdateRequestRepository updateRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<UpdateRequest> getRequests(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank() || "ALL".equalsIgnoreCase(statusFilter)) {
            return updateRequestRepository.findAllByOrderByCreatedAtDesc();
        }
        try {
            Status status = Status.valueOf(statusFilter.toUpperCase());
            return updateRequestRepository.findByStatusOrderByCreatedAtDesc(status);
        } catch (IllegalArgumentException e) {
            return updateRequestRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    @Override
    @Transactional
    public void approveRequest(Long requestId, String adminId) {
        UpdateRequest req = updateRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        if (req.getStatus() != Status.PENDING) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Employee emp = employeeRepository.findByEmployeeId(req.getEmployeeId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        applyFieldChange(emp, req.getFieldLabel(), req.getNewValue());
        employeeRepository.save(emp);

        req.setStatus(Status.APPROVED);
        req.setReviewedAt(LocalDateTime.now());
        updateRequestRepository.save(req);

        log.info("[ADMIN APPROVE] {} approved request {} for employee {}", adminId, requestId, req.getEmployeeId());
    }

    @Override
    @Transactional
    public void rejectRequest(Long requestId, String adminId, String adminNote) {
        UpdateRequest req = updateRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        if (req.getStatus() != Status.PENDING) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        req.setStatus(Status.REJECTED);
        req.setAdminNote(adminNote != null ? adminNote.trim() : "");
        req.setReviewedAt(LocalDateTime.now());
        updateRequestRepository.save(req);

        log.info("[ADMIN REJECT] {} rejected request {} for employee {}", adminId, requestId, req.getEmployeeId());
    }

    private void applyFieldChange(Employee emp, String fieldLabel, String newValue) {
        if (newValue == null) return;
        switch (fieldLabel) {
            case "Email" -> emp.setEmail(newValue);
            case "Email cá nhân" -> emp.setPersonalEmail(newValue);
            case "Số điện thoại" -> emp.setPhone(newValue);
            case "CCCD" -> emp.setIdCard(newValue);
            case "Ngày sinh" -> {
                try {
                    emp.setBirthDate(LocalDate.parse(newValue, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                } catch (Exception ignored) {}
            }
            case "Giới tính" -> emp.setGender(newValue);
            case "Dân tộc" -> emp.setEthnicity(newValue);
            case "Tỉnh/TP thường trú" -> emp.setProvince(newValue);
            case "Phường/Xã thường trú" -> emp.setDistrict(newValue);
            case "Tỉnh/TP tạm trú" -> emp.setTempProvince(newValue);
            case "Phường/Xã tạm trú" -> emp.setTempDistrict(newValue);
            case "Mã số thuế" -> emp.setTaxCode(newValue);
            case "Số tài khoản ngân hàng" -> emp.setBankAccount(newValue);
            case "Tên ngân hàng" -> emp.setBankName(newValue);
            case "Mã BHXH" -> emp.setInsuranceCode(newValue);
            default -> log.warn("[ADMIN] Unknown field label: {}", fieldLabel);
        }
    }
}
