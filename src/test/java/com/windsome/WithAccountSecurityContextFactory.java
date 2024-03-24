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
    private final MemberRepository memberRepository;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String userIdentifier = withAccount.value();

        Member member = Member.builder()
                .userIdentifier(userIdentifier)
                .name("홍길동")
                .password("test1234")
                .tel("01012341234")
                .email("email@naver.com")
                .role(Role.USER)
                .availablePoints(0)
                .totalEarnedPoints(0)
                .totalUsedPoints(0)
                .build();
        Member savedMember = memberRepository.save(member);

        if (Objects.equals(userIdentifier, "ADMIN")) {
            savedMember.setRole(Role.ADMIN);
            memberRepository.save(savedMember);
        }

        UserDetails principal = customUserDetailsService.loadUserByUsername(userIdentifier);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
