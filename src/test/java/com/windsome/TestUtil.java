package com.windsome;

import com.windsome.entity.Member;

public class TestUtil {

    public static Member createMember(Long accountId) {
        return Member.builder()
                .id(accountId)
                .userIdentifier("user1")
                .password("password")
                .name("John Doe")
                .email("user1@example.com")
                .address1("Address1")
                .address2("Address2")
                .address3("Address3")
                .point(1000)
                .totalPoint(1000)
                .totalUsePoint(0)
                .totalOrderPrice(0)
                .build();
    }
}
