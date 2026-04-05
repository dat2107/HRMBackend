package com.toto.backend.service.impl;

import com.toto.backend.dto.request.ChangePasswordRequest;
import com.toto.backend.dto.request.ForgotPasswordRequest;
import com.toto.backend.dto.request.LoginRequest;
import com.toto.backend.dto.response.LoginResponse;
import com.toto.backend.entity.Account;
import com.toto.backend.entity.Employee;
import com.toto.backend.enums.Role;
import com.toto.backend.exception.AppException;
import com.toto.backend.exception.ErrorCode;
import com.toto.backend.repository.AccountRepository;
import com.toto.backend.repository.EmployeeRepository;
import com.toto.backend.repository.SystemConfigRepository;
import com.toto.backend.security.JwtUtils;
import com.toto.backend.service.AuthService;
import com.toto.backend.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String CHARS_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String CHARS_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHARS_DIGIT = "0123456789";
    private static final String CHARS_SPECIAL = "!@#$%&*";

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String empId = request.getEmployeeId().trim().toUpperCase();

        if (!empId.matches("^(F0|M0)[0-9]{5}$")) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Account account = accountRepository.findByEmployeeId(empId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        checkAccountLockout(account);

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            handleFailedLogin(account);
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        account.setFailedAttempts(0);
        account.setIsLocked(false);
        account.setLockedUntil(null);
        account.setLastLogin(LocalDateTime.now());
        accountRepository.save(account);

        Employee employee = employeeRepository.findByEmployeeId(empId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtUtils.generateAccessToken(account);
        String refreshToken = jwtUtils.generateRefreshToken(account);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpireTime())
                .requireChangePassword(Boolean.TRUE.equals(account.getForceChangePwd()))
                .employee(LoginResponse.EmployeeBasicInfo.builder()
                        .employeeId(employee.getEmployeeId())
                        .fullName(employee.getFullName())
                        .department(employee.getDepartment())
                        .positionCode(employee.getPositionCode())
                        .email(employee.getEmail())
                        .role(account.getRole().name())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void logout(String employeeId) {
        accountRepository.incrementTokenVersion(employeeId);
        log.info("[LOGOUT] Token invalidated for {}", employeeId);
    }

    @Override
    @Transactional
    public void changePassword(String employeeId, ChangePasswordRequest request) {
        Account account = accountRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_OLD_PASSWORD);
        }

        if (passwordEncoder.matches(request.getNewPassword(), account.getPasswordHash())) {
            throw new AppException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        account.setForceChangePwd(false);
        account.setTokenVersion(account.getTokenVersion() + 1);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String empId = request.getEmployeeId().trim().toUpperCase();
        String inputEmail = request.getEmail().trim().toLowerCase();

        Employee employee = employeeRepository.findByEmployeeId(empId).orElse(null);

        if (employee == null) return;

        String systemEmail = getEmployeeEmail(employee);
        if (systemEmail == null || !systemEmail.toLowerCase().equals(inputEmail)) return;

        String newPassword = generateSecurePassword();

        Account account = accountRepository.findByEmployeeId(empId).orElse(null);
        if (account == null) {
            account = Account.builder()
                    .employeeId(empId)
                    .passwordHash(passwordEncoder.encode(newPassword))
                    .role(Role.USER)
                    .forceChangePwd(true)
                    .tokenVersion(0)
                    .build();
        } else {
            account.setPasswordHash(passwordEncoder.encode(newPassword));
            account.setForceChangePwd(true);
            account.setTokenVersion(account.getTokenVersion() + 1);
        }
        accountRepository.save(account);

        emailService.sendNewPasswordEmail(systemEmail, employee.getFullName(), empId, newPassword);
    }

    private void checkAccountLockout(Account account) {
        if (Boolean.TRUE.equals(account.getIsLocked())) {
            if (account.getLockedUntil() != null && LocalDateTime.now().isBefore(account.getLockedUntil())) {
                throw new AppException(ErrorCode.USER_NOT_ACTIVE);
            } else {
                account.setIsLocked(false);
                account.setFailedAttempts(0);
                account.setLockedUntil(null);
                accountRepository.save(account);
            }
        }
    }

    private void handleFailedLogin(Account account) {
        int maxAttempts = getSystemConfigInt("MAX_LOGIN_ATTEMPTS", 5);
        int lockoutSeconds = getSystemConfigInt("LOCKOUT_DURATION_SECONDS", 900);

        int attempts = account.getFailedAttempts() + 1;
        account.setFailedAttempts(attempts);

        if (attempts >= maxAttempts) {
            account.setIsLocked(true);
            account.setLockedUntil(LocalDateTime.now().plusSeconds(lockoutSeconds));
            log.warn("[SECURITY] Account {} locked after {} failed attempts", account.getEmployeeId(), attempts);
        }
        accountRepository.save(account);
    }

    private int getSystemConfigInt(String key, int defaultVal) {
        return systemConfigRepository.findByConfigKey(key)
                .map(c -> {
                    try { return Integer.parseInt(c.getConfigValue()); }
                    catch (Exception e) { return defaultVal; }
                })
                .orElse(defaultVal);
    }

    private String getEmployeeEmail(Employee employee) {
        if (employee.getEmail() != null && employee.getEmail().contains("@")) {
            return employee.getEmail().trim();
        }
        if (employee.getPersonalEmail() != null && employee.getPersonalEmail().contains("@")) {
            return employee.getPersonalEmail().trim();
        }
        return null;
    }

    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        char[] all = (CHARS_UPPER + CHARS_LOWER + CHARS_DIGIT + CHARS_SPECIAL).toCharArray();

        char[] pwd = new char[8];
        pwd[0] = CHARS_UPPER.charAt(random.nextInt(CHARS_UPPER.length()));
        pwd[1] = CHARS_LOWER.charAt(random.nextInt(CHARS_LOWER.length()));
        pwd[2] = CHARS_DIGIT.charAt(random.nextInt(CHARS_DIGIT.length()));
        pwd[3] = CHARS_SPECIAL.charAt(random.nextInt(CHARS_SPECIAL.length()));
        for (int i = 4; i < 8; i++) {
            pwd[i] = all[random.nextInt(all.length)];
        }

        for (int i = pwd.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = pwd[i]; pwd[i] = pwd[j]; pwd[j] = tmp;
        }
        return new String(pwd);
    }
}
