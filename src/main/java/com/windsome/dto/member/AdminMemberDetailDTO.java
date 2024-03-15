package com.windsome.dto.member;

import com.windsome.entity.member.Member;
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

    private String zipcode;

    private String addr;

    private String addrDetail;

    private int availablePoints;

    private int totalEarnedPoints;

    private int totalUsedPoints;

    public static AdminMemberDetailDTO toDto(Member member) {
        return AdminMemberDetailDTO.builder()
                .id(member.getId())
                .userIdentifier(member.getUserIdentifier())
                .password(member.getPassword())
                .name(member.getName())
                .email(member.getEmail())
                .zipcode(member.getZipcode())
                .addr(member.getAddr())
                .addrDetail(member.getAddrDetail())
                .availablePoints(member.getAvailablePoints())
                .totalEarnedPoints(member.getTotalEarnedPoints())
                .totalUsedPoints(member.getTotalUsedPoints())
                .build();
    }
}
