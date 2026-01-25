package com.barofarm.auth.application.usecase;

import com.barofarm.auth.domain.oauth.OAuthProvider;
import java.util.UUID;

public record OAuthLinkCallbackCommand(OAuthProvider provider, String code, String state, UUID userId) {
}
