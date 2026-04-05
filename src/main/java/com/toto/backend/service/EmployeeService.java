package com.toto.backend.service;

import com.toto.backend.dto.request.UpdateRequestDto;
import com.toto.backend.dto.response.EmployeeProfileResponse;
import com.toto.backend.entity.*;
import com.toto.backend.enums.Status;
import com.toto.backend.exception.AppException;
import com.toto.backend.exception.ErrorCode;
import com.toto.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UpdateRequestRepository updateRequestRepository;
    private final FieldConfigRepository fieldConfigRepository;
    private final ConfirmationRepository confirmationRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final MinioService minioService;

    private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORM = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public EmployeeProfileResponse getProfile(String employeeId) {
        Employee emp = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy pending requests
        List<UpdateRequest> pendingRequests = updateRequestRepository
                .findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, Status.PENDING);

        Map<String, String> pendingMap = new LinkedHashMap<>();
        for (UpdateRequest req : pendingRequests) {
            pendingMap.putIfAbsent(req.getFieldLabel(), req.getNewValue());
        }

        // Lấy field configs (xác định trường nào bị lock)
        List<FieldConfig> fieldConfigs = fieldConfigRepository.findAllByOrderByDisplayOrderAsc();
        Map<String, FieldConfig> configMap = new LinkedHashMap<>();
        for (FieldConfig fc : fieldConfigs) {
            configMap.put(fc.getFieldName().toLowerCase(), fc);
        }

        // Build field details
        List<EmployeeProfileResponse.FieldDetail> details = buildFieldDetails(emp, configMap, pendingMap);

        // Confirm status
        boolean isConfirmActive = "true".equalsIgnoreCase(
                systemConfigRepository.findByConfigKey("IS_CONFIRM_FEATURE_ACTIVE")
                        .map(SystemConfig::getConfigValue).orElse("false")
        );
        Optional<Confirmation> confirmation = confirmationRepository.findByEmployeeId(employeeId);
        String confirmedAt = confirmation
                .map(c -> c.getConfirmedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .orElse("");

        return EmployeeProfileResponse.builder()
                .baseInfo(EmployeeProfileResponse.BaseInfo.builder()
                        .id(emp.getEmployeeId())
                        .fullName(emp.getFullName())
                        .department(emp.getDepartment())
                        .positionCode(emp.getPositionCode())
                        .email(emp.getEmail())
                        .build())
                .details(details)
                .hasPending(!pendingMap.isEmpty())
                .confirmStatus(EmployeeProfileResponse.ConfirmStatus.builder()
                        .globalActive(isConfirmActive)
                        .userConfirmed(confirmation.isPresent())
                        .confirmedAt(confirmedAt)
                        .hasPending(!pendingMap.isEmpty())
                        .build())
                .build();
    }

    @Transactional
    public void submitUpdateRequest(String employeeId, UpdateRequestDto dto) {
        Employee emp = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String fieldLabel = dto.getFieldLabel().trim();

        // Kiểm tra trường có bị lock không
        Optional<FieldConfig> fc = fieldConfigRepository.findByFieldNameIgnoreCase(fieldLabel);
        if (fc.isPresent() && fc.get().getIsLocked() == 1) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // Xử lý file
        String fileUrl = "Không có tệp đính kèm";
        boolean isOptional = fc.map(f -> f.getIsOptionalUpload() == 1).orElse(false);

        if (dto.getFileData() != null && !dto.getFileData().isBlank()) {
            fileUrl = minioService.uploadBase64(dto.getFileData(), dto.getFileMimeType(), dto.getFileName());
        } else if (!isOptional) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        UpdateRequest request = UpdateRequest.builder()
                .employeeId(employeeId)
                .employeeName(emp.getFullName())
                .fieldLabel(fieldLabel)
                .oldValue(dto.getOldVal())
                .newValue(dto.getNewVal())
                .fileUrl(fileUrl)
                .status(Status.PENDING)
                .build();

        updateRequestRepository.save(request);
        log.info("[UPDATE REQUEST] {} submitted update for field '{}'", employeeId, fieldLabel);
    }

    @Transactional
    public void submitConfirmation(String employeeId) {
        if (confirmationRepository.existsByEmployeeId(employeeId)) {
            throw new AppException(ErrorCode.CONFLICT_DATA);
        }

        Employee emp = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Không cho confirm khi còn pending
        List<UpdateRequest> pending = updateRequestRepository
                .findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, Status.PENDING);
        if (!pending.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        confirmationRepository.save(Confirmation.builder()
                .employeeId(employeeId)
                .employeeName(emp.getFullName())
                .build());
    }

    private List<EmployeeProfileResponse.FieldDetail> buildFieldDetails(
            Employee emp,
            Map<String, FieldConfig> configMap,
            Map<String, String> pendingMap
    ) {
        List<EmployeeProfileResponse.FieldDetail> details = new ArrayList<>();

        // Các trường luôn cố định và thứ tự
        addField(details, "Mã nhân viên", "employeeId", emp.getEmployeeId(), "text", true, configMap, pendingMap);
        addField(details, "Họ và tên", "fullName", emp.getFullName(), "text", true, configMap, pendingMap);
        addField(details, "Phòng", "department", emp.getDepartment(), "text", true, configMap, pendingMap);
        addField(details, "Mã vị trí", "positionCode", emp.getPositionCode(), "text", true, configMap, pendingMap);
        addField(details, "Email", "email", emp.getEmail(), "text", false, configMap, pendingMap);
        addField(details, "Email cá nhân", "personalEmail", emp.getPersonalEmail(), "text", false, configMap, pendingMap);
        addField(details, "Số điện thoại", "phone", emp.getPhone(), "text", false, configMap, pendingMap);
        addField(details, "CCCD", "idCard", emp.getIdCard(), "text", false, configMap, pendingMap);
        addDateField(details, "Ngày sinh", "birthDate", emp.getBirthDate(), configMap, pendingMap);
        addField(details, "Giới tính", "gender", emp.getGender(), "dropdown", false, configMap, pendingMap);
        addField(details, "Dân tộc", "ethnicity", emp.getEthnicity(), "dropdown", false, configMap, pendingMap);
        addField(details, "Tỉnh/TP thường trú", "province", emp.getProvince(), "dropdown", false, configMap, pendingMap);
        addField(details, "Quận/Huyện thường trú", "district", emp.getDistrict(), "dropdown", false, configMap, pendingMap);
        addField(details, "Tỉnh/TP tạm trú", "tempProvince", emp.getTempProvince(), "dropdown", false, configMap, pendingMap);
        addField(details, "Quận/Huyện tạm trú", "tempDistrict", emp.getTempDistrict(), "dropdown", false, configMap, pendingMap);
        addField(details, "Mã số thuế", "taxCode", emp.getTaxCode(), "text", false, configMap, pendingMap);
        addField(details, "Số tài khoản ngân hàng", "bankAccount", emp.getBankAccount(), "text", false, configMap, pendingMap);
        addField(details, "Tên ngân hàng", "bankName", emp.getBankName(), "dropdown", false, configMap, pendingMap);
        addField(details, "Mã BHXH", "insuranceCode", emp.getInsuranceCode(), "text", false, configMap, pendingMap);
        addDateField(details, "Ngày vào làm", "startDate", emp.getStartDate(), configMap, pendingMap);

        return details;
    }

    private void addField(
            List<EmployeeProfileResponse.FieldDetail> list,
            String label, String key, String value, String inputType,
            boolean forceEditable,
            Map<String, FieldConfig> configMap,
            Map<String, String> pendingMap
    ) {
        if (value == null) value = "";

        FieldConfig fc = configMap.get(label.toLowerCase());
        boolean isLocked = fc != null && fc.getIsLocked() == 1;
        boolean editable = !forceEditable && !isLocked;

        // ID và tên luôn khóa
        if ("employeeId".equals(key) || "fullName".equals(key) || "department".equals(key) || "positionCode".equals(key)) {
            editable = false;
        }

        // Xử lý inputType từ config (dropdown nếu có dropdownValues)
        if (fc != null && fc.getDropdownValues() != null && !fc.getDropdownValues().isBlank()) {
            inputType = "dropdown";
        }

        boolean isPending = pendingMap.containsKey(label);
        String pendingVal = isPending ? pendingMap.get(label) : "";

        list.add(EmployeeProfileResponse.FieldDetail.builder()
                .key(key)
                .label(label)
                .value(value)
                .rawValue(value)
                .editable(editable)
                .inputType(inputType)
                .isPending(isPending)
                .pendingVal(pendingVal)
                .build());
    }

    private void addDateField(
            List<EmployeeProfileResponse.FieldDetail> list,
            String label, String key, java.time.LocalDate date,
            Map<String, FieldConfig> configMap,
            Map<String, String> pendingMap
    ) {
        String displayVal = date != null ? date.format(DATE_DISPLAY) : "";
        String formVal = date != null ? date.format(DATE_FORM) : "";

        FieldConfig fc = configMap.get(label.toLowerCase());
        boolean isLocked = fc != null && fc.getIsLocked() == 1;

        boolean isPending = pendingMap.containsKey(label);
        String pendingVal = isPending ? pendingMap.get(label) : "";

        list.add(EmployeeProfileResponse.FieldDetail.builder()
                .key(key)
                .label(label)
                .value(displayVal)
                .rawValue(formVal)
                .editable(!isLocked)
                .inputType("date")
                .isPending(isPending)
                .pendingVal(pendingVal)
                .build());
    }
}
