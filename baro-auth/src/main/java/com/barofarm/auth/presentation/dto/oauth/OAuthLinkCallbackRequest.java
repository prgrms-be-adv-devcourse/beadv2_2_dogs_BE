package com.barofarm.auth.presentation.dto.oauth;

import com.barofarm.auth.application.usecase.OAuthLinkCallbackCommand;
import com.barofarm.auth.domain.oauth.OAuthProvider;
import com.barofarm.auth.exception.AuthErrorCode;
import com.barofarm.exception.CustomException;
import java.util.UUID;

public record OAuthLinkCallbackRequest(
//     @Schema(description = "OAuth 공급자", example = "naver")
     String provider,
//     @Schema(description = "Authorization code")
     String code,
//     @Schema(description = "OAuth link state")
     String state
) {

    public OAuthLinkCallbackCommand toServiceRequest(UUID userId) {
        OAuthProvider resolved = OAuthProvider.from(provider);
        if (resolved == null) {
            throw new CustomException(AuthErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        }
        return new OAuthLinkCallbackCommand(resolved, code, state, userId);
    }
}
