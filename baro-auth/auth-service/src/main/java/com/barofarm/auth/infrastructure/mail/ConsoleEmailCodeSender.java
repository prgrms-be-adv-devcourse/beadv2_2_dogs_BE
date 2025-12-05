package com.barofarm.auth.infrastructure.mail;

import com.barofarm.auth.application.port.out.EmailCodeSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// 구현 어댑터

@Component
public class ConsoleEmailCodeSender implements EmailCodeSender {

  private static final Logger LOG = LoggerFactory.getLogger(ConsoleEmailCodeSender.class);

  @Override
  public void send(String email, String code) {
    // TODO: 메일 전송 로직 향후 구현
    LOG.info("[ConsoleEmailCodeSender] Sending email code {} to {}", code, email);
  }
}
