package com.barofarm.auth.infrastructure.oauth;

import com.barofarm.auth.application.port.out.OAuthProviderClient;
import com.barofarm.auth.domain.oauth.OAuthProvider;
import com.barofarm.auth.domain.oauth.OAuthUserInfo;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DelegatingOAuthProviderClient implements OAuthProviderClient {

    private final Map<OAuthProvider, OAuthProviderHandler> handlers;

    public DelegatingOAuthProviderClient(List<OAuthProviderHandler> handlers) {
        // 공급자별 핸들러를 맵으로 구성해 호출부가 공급자별 분기에서 벗어나도록 한다.
        this.handlers = new EnumMap<>(OAuthProvider.class);
        for (OAuthProviderHandler handler : handlers) {
            this.handlers.put(handler.provider(), handler);
        }
    }

    @Override
    public OAuthUserInfo fetchUserInfo(OAuthProvider provider, String code, String state) {
        OAuthProviderHandler handler = handlers.get(provider);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }
        return handler.fetchUserInfo(code, state);
    }
}
