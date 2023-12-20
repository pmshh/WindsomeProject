package com.windsome;

import com.windsome.account.AccountService;
import com.windsome.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class WithAccountSecurityContextFacotry implements WithSecurityContextFactory<WithAccount> {

    private final AccountService accountService;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String userIdentifier = withAccount.value();

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUserIdentifier(userIdentifier);
        signUpForm.setEmail("pms000723@gmail.com");
        signUpForm.setName("홍길동");
        signUpForm.setPassword("test1234");
        signUpForm.setAddress1("test");
        signUpForm.setAddress2("test");
        signUpForm.setAddress3("test");
        accountService.processNewAccount(signUpForm);

        UserDetails principal = accountService.loadUserByUsername(userIdentifier);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
