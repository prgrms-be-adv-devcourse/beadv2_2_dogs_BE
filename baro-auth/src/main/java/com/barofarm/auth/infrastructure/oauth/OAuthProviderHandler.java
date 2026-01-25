package com.barofarm.auth.infrastructure.oauth;

import com.barofarm.auth.domain.oauth.OAuthProvider;
import com.barofarm.auth.domain.oauth.OAuthUserInfo;

public interface OAuthProviderHandler {

    // 이 핸들러가 담당하는 OAuth 공급자를 식별한다.
    OAuthProvider provider();

    // 공급자별 인가 코드로 사용자 정보를 조회한다.
    OAuthUserInfo fetchUserInfo(String code, String state);
}
