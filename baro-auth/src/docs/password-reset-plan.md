# 비밀번호 재설정/변경 계획

## 목표
- 인증 코드 기반 비밀번호 재설정(비로그인 상태)과 로그인 사용자의 비밀번호 변경 API 제공.
- 기존 이메일 인증 코드 인프라 재사용.
- 비밀번호 변경 시 기존 리프레시 토큰 폐기로 세션 무효화.

## 엔드포인트 설계
- `POST /auth/password/reset/request`
  - Body: `{ "email": "user@example.com" }`
  - 동작: 가입된 이메일 여부 확인 후 인증 코드 발송.
- `POST /auth/password/reset/confirm`
  - Body: `{ "email": "...", "code": "123456", "newPassword": "N3wP@ss!" }`
  - 동작: 코드 검증 → 비밀번호/소금 재생성 → 자격 증명 업데이트 → 해당 사용자 리프레시 토큰 삭제.
- `POST /auth/password/change` (인증 필요)
  - Body: `{ "currentPassword": "...", "newPassword": "..." }`
  - 동작: 현재 비밀번호 검증 → 새 비밀번호/소금 재생성 → 자격 증명 업데이트 → 리프레시 토큰 삭제.

## 플로우
1) 재설정 요청: 이메일 존재 확인 → `EmailVerificationService.sendVerification`.
2) 재설정 확정: `verifyCode`로 코드 확인·검증 상태 설정 → `ensureVerified`로 최신 기록 정리 → 암호 해시/소금 갱신 → `deleteAllByUserId`로 세션 폐기.
3) 로그인 사용자의 변경: 현재 비밀번호 매칭 → 암호 해시/소금 갱신 → 리프레시 토큰 폐기.

## 데이터/도메인 변경
- `AuthCredential.changePassword(hash, salt)` 추가.
- `AuthCredentialJpaRepository.findByUserId` 추가.
- `UserJpaRepository.findByEmail` 추가(향후 필요 시 활용).

## 보안/검증 메모
- 코드 검증 실패/만료 시 400, 존재하지 않는 이메일/자격 증명 시 404, 현재 비밀번호 불일치 시 401.
- 비밀번호 변경/재설정 시 모든 리프레시 토큰 삭제로 재로그인 강제.
- 비밀번호 정책(길이/복잡도)은 현재 서비스 단 검증 없음 → 필요 시 DTO/서비스에서 추가 검증 가능.

## 추후 작업 아이디어
- 인증 코드 전송 시 레이트 리밋/캡차.
- 코드 유효 시간/재요청 쿨다운 설정.
- 비밀번호 변경 이력/감사 로그 적재.
