package com.barofarm.auth.api.dto;

import com.barofarm.auth.application.dto.LoginCommand;

// 나중에 API v2 를 만들거나, gRPC/메시지 큐를 붙여도 추가해서 쓰면되는?
// api.dto 는 HTTP/JSON 전용 모델
// @RequestBody, @Schema, 프론트 요구사항에 맞는 필드 이름

public record LoginRequest(String email, String password) {

  public LoginCommand toServiceRequest() {
    return new LoginCommand(email, password);
  }
}
