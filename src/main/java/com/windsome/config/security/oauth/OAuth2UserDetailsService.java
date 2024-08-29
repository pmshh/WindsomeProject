package com.windsome.config.security.oauth;

import com.windsome.constant.Role;
import com.windsome.dto.oauth.KakaoResponse;
import com.windsome.dto.oauth.NaverResponse;
import com.windsome.dto.oauth.OAuth2Response;
import com.windsome.entity.member.Address;
import com.windsome.entity.member.Member;
import com.windsome.exception.AccountDeactivatedException;
import com.windsome.repository.member.AddressRepository;
import com.windsome.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserDetailsService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String userIdentifier = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
        Member existData = memberRepository.findByUserIdentifier(userIdentifier);

        if (existData != null && existData.isDeleted()) {
            throw new AccountDeactivatedException("계정이 비활성화되었습니다.<br>자세한 사항은 관리자에게 문의하세요.");
        }

        Role role = Role.USER;
        if (existData == null) {

            Member member = new Member();
            member.setUserIdentifier(userIdentifier);
            member.setName(oAuth2Response.getName());
            member.setEmail(oAuth2Response.getEmail());
            member.setTel("");
            member.setOauth(request.getClientRegistration().getClientName());
            member.setRole(role);

            memberRepository.save(member);
        } else {

            existData.setUserIdentifier(userIdentifier);
            existData.setName(oAuth2Response.getName());
            existData.setEmail(oAuth2Response.getEmail());

            role = existData.getRole();

            memberRepository.save(existData);
        }

        // 저장된 회원 정보 가져오기
        Member savedMember = memberRepository.findByUserIdentifier(userIdentifier);

        // 기본 배송지 정보가 없으면 저장
        if (!addressRepository.existsByMemberIdAndIsDefault(savedMember.getId(), true)) {
            addressRepository.save(Address.builder()
                    .member(savedMember)
                    .name(oAuth2Response.getName())
                    .tel("")
                    .zipcode("")
                    .addr("")
                    .addrDetail("")
                    .req("")
                    .isDefault(true)
                    .build());
        }
        String memberRole = role.toString();
        return new CustomOAuth2User(oAuth2Response, memberRole, savedMember);
    }
}
