package com.barofarm.auth.infrastructure.security;

import com.barofarm.auth.common.exception.CustomException;
import com.barofarm.auth.domain.user.User;
import com.barofarm.auth.exception.AuthErrorCode;
import com.barofarm.auth.infrastructure.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws CustomException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        String role = user.getUserType().name();

        // UserDetails principal을 AuthUserPrincipal로 전달해 userId/role을 컨트롤러에서 활용
        return new AuthUserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getPhone(),
            user.isMarketingConsent(),
            user.getUserType());
    }
}
