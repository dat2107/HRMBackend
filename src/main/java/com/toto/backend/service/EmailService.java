package com.toto.backend.service;

public interface EmailService {
    void sendNewPasswordEmail(String to, String employeeName, String empId, String newPassword);
}
