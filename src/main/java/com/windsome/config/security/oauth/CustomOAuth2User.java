package com.windsome.config.security.oauth;


import com.windsome.dto.oauth.OAuth2Response;
import com.windsome.entity.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User  implements OAuth2User {

    private final OAuth2Response oAuth2Response;
    private final String role;
    private final Member member;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new SimpleGrantedAuthority("ROLE_" + role));
        return collection;
    }

    @Override
    public String getName() {

        return oAuth2Response.getName();
    }

    public String getUsername() {

        return oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
    }

    public Member getMember() {

        return member;
    }

}
