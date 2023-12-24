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
public class WithAccountSecurityContextFacotry implements WithSecurityContextFactory<WithAccount> {

    private final CustomUserDetailsService customUserDetailsService;
    private final AccountService accountService;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String userIdentifier = withAccount.value();

        SignUpFormDto signUpFormDto = new SignUpFormDto();
        signUpFormDto.setUserIdentifier(userIdentifier);
        signUpFormDto.setEmail("pms000723@gmail.com");
        signUpFormDto.setName("홍길동");
        signUpFormDto.setPassword("test1234");
        signUpFormDto.setAddress1("test");
        signUpFormDto.setAddress2("test");
        signUpFormDto.setAddress3("test");
        accountService.processNewAccount(signUpFormDto);

        UserDetails principal = customUserDetailsService.loadUserByUsername(userIdentifier);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
