package com.barofarm.auth.infrastructure.mail;

import com.barofarm.auth.application.port.out.EmailCodeSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsoleEmailCodeSender implements EmailCodeSender {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleEmailCodeSender.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:}")
    private String from;

    @Override
    public void send(String email, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            LOG.info("[ConsoleEmailCodeSender] JavaMailSender bean not found. email={}, code={}", email, code);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setTo(email);
            helper.setSubject("[Baro-Farm] 이메일 인증 코드");
            helper.setText(buildBody(code), false);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("메일 메시지 생성 실패", e);
        } catch (Exception e) {
            throw new MailSendException("메일 전송 실패", e);
        }

        LOG.info("[ConsoleEmailCodeSender] Sending email code {} to {}", code, email);
    }

    private String buildBody(String code) {
        return """
               안녕하세요.

               아래 인증 코드를 입력해주세요.
               %s

               본 메일은 발신 전용입니다.
               """.formatted(code);
    }
}
