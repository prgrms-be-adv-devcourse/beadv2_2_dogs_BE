## Refresh token PK 변경 요약

- 의도: 사용자당 리프레시 토큰 1개만 갖도록 DB 차원에서 강제.
- 엔티티: `RefreshToken`의 PK를 `userId`로 변경하고 `id` 컬럼을 제거했습니다. 회전 시 `rotate(token, expiresAt)`로 같은 행을 덮어씁니다.
- 서비스: `/auth/refresh`는 더 이상 기존 토큰을 `revoked`로 남기지 않고, 새 토큰/만료 시각으로 해당 행을 교체합니다. `/auth/signup`·`/auth/login`의 기존 `deleteAllByUserId` + save 패턴은 그대로 동작해 단일 행을 유지합니다.
- 테스트: 단일 행 교체에 맞게 통합 테스트와 `AuthServiceTest`를 수정했습니다.

## DB 마이그레이션 메모

1) 기존 데이터에서 사용자별 최신 토큰 1개만 남기고 정리합니다.  
2) `refresh_token` 테이블에서 `id` 컬럼을 드롭하고 `user_id`를 PK로 설정합니다. (예: `ALTER TABLE refresh_token DROP COLUMN id; ALTER TABLE refresh_token ADD PRIMARY KEY (user_id);`)  
3) `user_id`가 기존과 동일한 `BINARY(16)`/UUID 형태인지 확인합니다.  
4) 애플리케이션 배포 후 `/auth/refresh`가 단일 행을 업데이트하는지 로그/DB에서 점검합니다.
