package com.windsome;

import com.windsome.constant.Role;
import com.windsome.dto.member.SignUpRequestDTO;
import com.windsome.entity.member.Member;
import com.windsome.repository.member.MemberRepository;
import com.windsome.service.member.MemberService;
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
                .tel("01012341234")
                .zipcode("test")
                .addr("test")
                .addrDetail("test")
                .build();
        memberService.createAccount(signUpRequestDTO);

        Member member = memberRepository.findByUserIdentifier(userIdentifier);
        member.setRole(Role.ADMIN);
        memberRepository.save(member);

        if (Objects.equals(userIdentifier, "USER")) {
            member.setRole(Role.USER);
            memberRepository.save(member);
        } else if (Objects.equals(userIdentifier, "ADMIN")) {
            member.setRole(Role.ADMIN);
            memberRepository.save(member);
        }

        UserDetails principal = customUserDetailsService.loadUserByUsername(userIdentifier);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
