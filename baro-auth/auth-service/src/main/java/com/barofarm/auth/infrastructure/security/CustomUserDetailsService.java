package com.barofarm.auth.infrastructure.security;

import com.barofarm.auth.api.exception.BusinessException;
import com.barofarm.auth.domain.credential.AuthCredential;
import com.barofarm.auth.infrastructure.jpa.AuthCredentialJpaRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.http.HttpStatus;
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
  public UserDetails loadUserByUsername(String email) throws BusinessException {
    AuthCredential credential =
        credentialRepository
            .findByLoginEmail(email)
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_GATEWAY, "" + email));

    // userType / role 은 나중에 확장 가능
    Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    return new org.springframework.security.core.userdetails.User(
        credential.getLoginEmail(), credential.getPasswordHash(), authorities);
  }
}
