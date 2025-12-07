## 헷갈리는 것 정리 : Verification-Authentication-Authorization

### Verification (이메일 인증)
- "이 이메일/휴대폰 번호가 진짜 해당 인물 게 맞는지 확인하는 절차"
- 구현 :
  - 코드 발송
  - 코드 검증
- 주의 : 
  - 여기선 아직 계정이 안만들어지고, 코드만 맞는지 확인하는 단계 있을 수 있음
  - 여기서 최종적으로 회원가입이 이뤄짐.  


### Authentication (로그인)
- "이 사람이 이미 등록된 계정의 주인인지 확인하는 단계"
- 구현 :
    - 이메일과 패스워드 검증
    - 맞으면 JWT 발급해주기

### Authorization (인가)
- "이 사람이 어떤 기능을 쓸 수 있는지 (단순회원인지, 판매자인지)"
- 구현 :
    - Spring Security + JWT 에서 ROLE_USER, ROLL_SELLER 같은 권한으로 처리
    - 특정 API에서 `@PreAuthorize("hasRole("SELLER))` 이런식으로 할 수 있음

---

### 컨트롤러 구조
```
api
├─ AuthController          // signup, login, /me 등
└─ VerificationController  // /auth/verification/email/** (코드 발송/검증)
```

### 대략적인 흐름

1. 이메일 인증 코드 발송 & 검증 (VerificationController에서 처리)
2. 회원가입 (AuthController에서)
   - 우선은 Veri.control.에서 ../verify로 보내서 인증 완료
   - 그 후에 /auth/signup 호출
   - Auth관련 Service에서 signup
   - User 저장하고, AuthCredential 저장
   - JwtTokenProvider로 accessToken+refresh 토큰 발급
   - TokenResponse 반환하기
3. 로그인 (AuthController에서)
   - AuthService에서
   - findByLoginEmail()하고
   - passwordEncoder.matches(rawPw, passwordHash)
   - 성공하면 토큰
4. JWT 검증하고 인가(Spring Security)
```aiignore
예)
http
    .csrf(csrf -> csrf.disable())
    .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .exceptionHandling(e ->
            e.authenticationEntryPoint(authenticationEntryPoint))
    .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/auth/signup",
                "/auth/login",
                "/auth/verification/**"
            ).permitAll()
            .anyRequest().authenticated()
    )
    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

```
---

251207 20:40 기준 할거 (-10시 까지)
0. 지금 된거 : 이메일 인증
1. 목표1 . AuthService + 테스트 완성 (V)
2. 목표2 . AuthController에 /auth/signup 연결 (V)
3. 이후:
    - JWT 발급/검증(V)
    - /auth/login, /auth/me 구현 (v)
    - 다른 서비스 (buyer-service, seller-service에서) JWT 기반 인증 연동하기

4. 토큰 담는 곳 아직 구현 안함 -> 이후 이거 이용해서 게이트웨이 및 buyer / seller랑
5. AOP, Gateway관련
