package com.windsome;

import com.windsome.constant.Role;
import com.windsome.dto.member.SignUpRequestDTO;
import com.windsome.entity.Member;
import com.windsome.repository.member.MemberRepository;
import com.windsome.service.MemberService;
import com.windsome.config.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Objects;

@RequiredArgsConstructor
public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {

    private final CustomUserDetailsService customUserDetailsService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String userIdentifier = withAccount.value();

        SignUpRequestDTO signUpRequestDTO = SignUpRequestDTO.builder()
                .userIdentifier(userIdentifier)
                .email(userIdentifier + "@email.com")
                .name("홍길동")
                .password("test1234")
                .address1("test")
                .address2("test")
                .address3("test")
                .build();
        memberService.createAccount(signUpRequestDTO);

        Member member = memberRepository.findByUserIdentifier(userIdentifier);
        member.setState(Role.ADMIN);
        memberRepository.save(member);

        if (Objects.equals(userIdentifier, "USER")) {
            member.setState(Role.USER);
            memberRepository.save(member);
        } else if (Objects.equals(userIdentifier, "ADMIN")) {
            member.setState(Role.ADMIN);
            memberRepository.save(member);
        }

        UserDetails principal = customUserDetailsService.loadUserByUsername(userIdentifier);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
