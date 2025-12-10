package com.barofarm.auth.application.port.out;

/** 실제 구현은 SMTP, Amazon SES, Mailgun 등 사용. 테스트에서는 이 인터페이스를 mock 으로 대체. */
public interface EmailCodeSender {

    void send(String email, String code);
}
