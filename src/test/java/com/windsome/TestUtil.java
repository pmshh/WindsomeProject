package com.windsome;

import com.windsome.constant.Role;
import com.windsome.entity.member.Member;

public class TestUtil {

    public static Member createMember(Long accountId) {
        return Member.builder()
                .id(accountId)
                .userIdentifier("user1")
                .password("password")
                .name("John Doe")
                .email("user1@example.com")
                .zipcode("test")
                .addr("test")
                .addrDetail("test")
                .role(Role.USER)
                .availablePoints(0)
                .totalUsedPoints(0)
                .totalEarnedPoints(0)
                .build();
    }
}
