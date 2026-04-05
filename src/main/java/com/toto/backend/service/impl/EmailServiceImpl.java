package com.toto.backend.service.impl;

import com.toto.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendNewPasswordEmail(String to, String employeeName, String empId, String newPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Human Resource");
            helper.setTo(to);
            helper.setSubject("HRM - Thông báo cập nhật thông tin tài khoản - " + employeeName + " (" + empId + ")");

            String html = buildPasswordEmailHtml(employeeName, newPassword);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("[EMAIL] Sent new password to {} ({})", empId, obscureEmail(to));
        } catch (Exception e) {
            log.error("[EMAIL ERROR] Failed to send to {}: {}", to, e.getMessage());
        }
    }

    private String buildPasswordEmailHtml(String name, String password) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;font-family:'Segoe UI',sans-serif;background:#f4f7f6">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f7f6">
                  <tr><td align="center" style="padding:20px 0">
                    <table width="600" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:4px;overflow:hidden;box-shadow:0 2px 5px rgba(0,0,0,.05);border:1px solid #e0e0e0">
                      <tr><td style="background:#234699;color:#fff;text-align:center;padding:15px;font-size:16px;font-weight:bold;letter-spacing:1px">
                        CỔNG THÔNG TIN NHÂN SỰ TVN
                      </td></tr>
                      <tr><td style="padding:20px 30px 5px;border-bottom:1px solid #eee">
                        <h2 style="margin:0;color:#234699;font-size:18px">HỖ TRỢ TRUY CẬP CỔNG THÔNG TIN</h2>
                      </td></tr>
                      <tr><td style="padding:15px 30px 25px;font-size:14px;line-height:1.6;color:#333">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Cổng thông tin ghi nhận yêu cầu hỗ trợ truy cập tài khoản của bạn.</p>
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f1f4f9;border-radius:8px;margin:20px 0">
                          <tr><td align="center" style="padding:20px">
                            <div style="font-size:13px;color:#555;margin-bottom:10px">Mã đăng nhập tạm thời:</div>
                            <div style="font-family:'Courier New',monospace;font-size:24px;font-weight:bold;color:#234699;background:#fff;padding:10px 20px;border-radius:4px;border:1px solid #d1d9e6">
                              %s
                            </div>
                          </td></tr>
                        </table>
                        <p><strong>Hướng dẫn:</strong></p>
                        <ul style="color:#333;padding-left:20px;line-height:1.5">
                          <li>Sử dụng mã trên để đăng nhập tại Cổng thông tin.</li>
                          <li>Sau khi đăng nhập, hệ thống sẽ yêu cầu bạn <strong>thiết lập mật khẩu mới</strong>.</li>
                        </ul>
                      </td></tr>
                      <tr><td style="background:#f8f9fa;text-align:center;padding:10px;font-size:11px;color:#888;border-top:1px solid #eee">
                        <p style="margin:2px 0">Email này được gửi tự động. Vui lòng không trả lời.</p>
                        <p style="margin:2px 0">&copy; TOTO Vietnam Co., Ltd.</p>
                      </td></tr>
                    </table>
                  </td></tr>
                </table>
                </body></html>
                """.formatted(name, password);
    }

    private String obscureEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        return parts[0].substring(0, Math.min(3, parts[0].length())) + "***@" + parts[1];
    }
}
