package com.windsome.config.security;

import com.windsome.entity.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UserAccount extends User {

    private Account account;

    public UserAccount(Account account) {
        // User 정보 등록
        super(account.getUserIdentifier(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + account.getState())));
        this.account = account;
    }
}
