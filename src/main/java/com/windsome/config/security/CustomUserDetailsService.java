package com.windsome.config.security;

import com.windsome.repository.AccountRepository;
import com.windsome.config.security.UserAccount;
import com.windsome.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    // 로그인 처리할 때 DB 정보를 조회해서 로그인 처리할 수 있도록 UserDetailsService를 구현해야 한다.
    // loadUserbyUsername 메서드는 UserDetailsService의 필수 메서드이다.
    @Override
    public UserDetails loadUserByUsername(String userIdOrEmail) throws UsernameNotFoundException {
        // id로 조회
        Account account = accountRepository.findByUserIdentifier(userIdOrEmail);
        if (account == null) {
            // email로 조회
            account = accountRepository.findByEmail(userIdOrEmail);
        }

        if (account == null) {
            throw new UsernameNotFoundException(userIdOrEmail);
        }

        return new UserAccount(account);
    }
}