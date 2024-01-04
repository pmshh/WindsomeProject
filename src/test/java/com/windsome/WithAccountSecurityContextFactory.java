package com.windsome;

import com.windsome.dto.SignUpFormDto;
import com.windsome.service.AccountService;
import com.windsome.config.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {

    private final CustomUserDetailsService customUserDetailsService;
    private final AccountService accountService;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String userIdentifier = withAccount.value();

        SignUpFormDto signUpFormDto = SignUpFormDto.builder()
                .userIdentifier(userIdentifier)
                .email("pms000723@gmail.com")
                .name("test")
                .password("test1234")
                .address1("test")
                .address2("test")
                .address3("test")
                .build();
        accountService.saveNewAccount(signUpFormDto);

        UserDetails principal = customUserDetailsService.loadUserByUsername(userIdentifier);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
