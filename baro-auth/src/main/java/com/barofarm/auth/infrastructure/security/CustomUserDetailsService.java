package com.barofarm.auth.infrastructure.security;

import com.barofarm.auth.common.exception.CustomException;
import com.barofarm.auth.domain.credential.AuthCredential;
import com.barofarm.auth.exception.AuthErrorCode;
import com.barofarm.auth.infrastructure.jpa.AuthCredentialJpaRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthCredentialJpaRepository credentialRepository;

    public CustomUserDetailsService(AuthCredentialJpaRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws CustomException {
        AuthCredential credential = credentialRepository.findByLoginEmail(email)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // userType / role 는 추후 확장 고려
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // UserDetails principal을 AuthUserPrincipal로 전달해 userId/role을 컨트롤러에서 활용
        return new AuthUserPrincipal(credential.getUserId(), credential.getLoginEmail(), "USER");
    }
}
