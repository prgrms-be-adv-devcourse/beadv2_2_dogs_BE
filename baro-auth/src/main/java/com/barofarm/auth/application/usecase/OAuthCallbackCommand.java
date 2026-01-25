package com.barofarm.auth.application.usecase;

import com.barofarm.auth.domain.oauth.OAuthProvider;

public record OAuthCallbackCommand(OAuthProvider provider, String code, String state) {
}
