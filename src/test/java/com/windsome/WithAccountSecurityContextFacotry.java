package com.windsome;

import com.windsome.dto.SignUpDto;
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

        SignUpDto signUpDto = new SignUpDto();
        signUpDto.setUserIdentifier(userIdentifier);
        signUpDto.setEmail("pms000723@gmail.com");
        signUpDto.setName("홍길동");
        signUpDto.setPassword("test1234");
        signUpDto.setAddress1("test");
        signUpDto.setAddress2("test");
        signUpDto.setAddress3("test");
        accountService.processNewAccount(signUpDto);

        UserDetails principal = customUserDetailsService.loadUserByUsername(userIdentifier);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
