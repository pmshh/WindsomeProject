package com.windsome.dto.oauth;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes; // 사용자 속성 정보를 담고 있는 맵

    // 사용자 속성 정보 초기화
    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // 제공자 이름 반환 (예: kakao, naver, google 등)
    @Override
    public String getProvider() {
        return "kakao";
    }

    // 제공자로부터 발급된 고유 사용자 ID 반환
    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    // 사용자 이메일 반환
    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount.get("email").toString();
    }

    // 사용자 이름 반환
    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties.get("nickname").toString();
    }
}
