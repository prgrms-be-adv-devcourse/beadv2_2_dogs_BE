package com.barofarm.auth.application.port.out;

import com.barofarm.auth.domain.oauth.OAuthProvider;
import com.barofarm.auth.domain.oauth.OAuthUserInfo;

/**
 * 외부 OAuth 공급자와 통신하는 포트.
 * 실제 HTTP 호출은 인프라 계층이 담당한다.
 */
public interface OAuthProviderClient {

    OAuthUserInfo fetchUserInfo(OAuthProvider provider, String code, String state);
}
