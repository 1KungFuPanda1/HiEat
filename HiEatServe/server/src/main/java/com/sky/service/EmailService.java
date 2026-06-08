package com.sky.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务
 * 开发阶段：验证码发送到固定QQ邮箱（替代短信）
 * 生产环境：可替换为短信服务商API
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送验证码邮件（开发测试用，发送到固定邮箱）
     *
     * @param phone 用户手机号
     * @param code  验证码
     */
    public void sendVerificationCode(String phone, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(from); // 开发阶段：发给自己（固定邮箱）
            message.setSubject("【HiEat】验证码 - 手机号 " + phone);
            message.setText(
                    "【HiEat 智慧餐饮】\n\n"
                            + "手机号：" + phone + " 正在登录\n"
                            + "验证码：" + code + "\n"
                            + "有效期：5 分钟\n\n"
                            + "如非本人操作，请忽略此邮件。"
            );

            mailSender.send(message);
            log.info("验证码邮件发送成功：手机号={}, 验证码={}", phone, code);
        } catch (Exception e) {
            log.error("验证码邮件发送失败：手机号={}, 错误={}", phone, e.getMessage());
            throw new RuntimeException("验证码邮件发送失败，请稍后重试");
        }
    }
}
