package com.windsome.config.security;

import com.windsome.entity.Member;
import com.windsome.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 로그인 처리할 때 DB 정보를 조회해서 로그인 처리할 수 있도록 UserDetailsService를 구현해야 한다.
    // loadUserbyUsername 메서드는 UserDetailsService의 필수 메서드이다.
    @Override
    public UserDetails loadUserByUsername(String userIdOrEmail) throws UsernameNotFoundException {
        // id로 조회
        Member member = memberRepository.findByUserIdentifier(userIdOrEmail);
        if (member == null) {
            // email로 조회
            member = memberRepository.findByEmail(userIdOrEmail);
        }

        if (member == null) {
            throw new UsernameNotFoundException(userIdOrEmail);
        }

        return new memberAccount(member);
    }
}
