package com.windsome.config.security.oauth;

import com.windsome.constant.Role;
import com.windsome.entity.member.Address;
import com.windsome.entity.member.Member;
import com.windsome.repository.member.AddressRepository;
import com.windsome.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserDetailsService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final HttpSession httpSession;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String oauthClientName = request.getClientRegistration().getClientName();
        String accessToken = request.getAccessToken().getTokenValue();

        // Access token을 세션에 저장
        httpSession.setAttribute("oauthAccessToken", accessToken);

        String userIdentifier = null;
        Member member = null;
        String name = null;
        String email = null;

        if ("kakao".equals(oauthClientName)) {
            Map<String, Object> kakaoAttributes = oAuth2User.getAttribute("kakao_account");
            userIdentifier = "kakao_" + kakaoAttributes.get("id");
            name = ((Map<String, String>) oAuth2User.getAttribute("properties")).get("nickname");
            email = (String) kakaoAttributes.get("email");
        } else if ("naver".equals(oauthClientName)) {
            Map<String, String> responseMap = oAuth2User.getAttribute("response");
            userIdentifier = "naver_" + responseMap.get("id").substring(0,14);
            name = responseMap.get("name");
            email = responseMap.get("email");
        }

        boolean result = memberRepository.existsByUserIdentifier(userIdentifier);
        if (!result) {
            member = Member.builder()
                    .name(name)
                    .userIdentifier(userIdentifier)
                    .password(passwordEncoder.encode(userIdentifier))
                    .email(email)
                    .tel("")
                    .role(Role.USER)
                    .oauth(oauthClientName)
                    .build();

            // 회원 정보 저장
            memberRepository.save(member);
        }

        // 저장된 회원 정보 가져오기
        Member savedMember = memberRepository.findByUserIdentifier(userIdentifier);

        // 기본 배송지 정보가 없으면 저장
        if (!addressRepository.existsByMemberIdAndIsDefault(savedMember.getId(), true)) {
            addressRepository.save(Address.builder()
                    .member(savedMember)
                    .name(name)
                    .tel("")
                    .zipcode("")
                    .addr("")
                    .addrDetail("")
                    .req("")
                    .isDefault(true)
                    .build());
        }

        return new CustomOAuth2User(savedMember);
    }
}
