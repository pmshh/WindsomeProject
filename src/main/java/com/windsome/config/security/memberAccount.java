package com.windsome.config.security;

import com.windsome.entity.Member;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class memberAccount extends User {

    private Member member;

    public memberAccount(Member member) {
        // User 정보 등록
        super(member.getUserIdentifier(), member.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole())));
        this.member = member;
    }
}
