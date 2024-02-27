package com.windsome.dto.member;

import com.windsome.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class AdminMemberDetailDTO {

    private Long id;

    private String userIdentifier;

    private String password;

    private String name;

    private String email;

    private String address1;

    private String address2;

    private String address3;

    private int point;

    private int totalPoint;

    private int totalUsePoint;

    private int totalOrderPrice;

    public static AdminMemberDetailDTO toDto(Member member) {
        return AdminMemberDetailDTO.builder()
                .id(member.getId())
                .userIdentifier(member.getUserIdentifier())
                .email(member.getEmail())
                .name(member.getName())
                .password(member.getPassword())
                .address1(member.getAddress1())
                .address2(member.getAddress2())
                .address3(member.getAddress3())
                .point(member.getPoint())
                .totalPoint(member.getTotalPoint())
                .totalUsePoint(member.getTotalUsePoint())
                .totalOrderPrice(member.getTotalOrderPrice())
                .build();
    }
}
