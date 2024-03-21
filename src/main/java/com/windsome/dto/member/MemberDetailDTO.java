package com.windsome.dto.member;

import com.windsome.entity.member.Address;
import com.windsome.entity.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberDetailDTO {

    private Long memberId;

    private String userIdentifier;

    private String name;

    private String password;

    private String passwordConfirm;

    private String email;

    private String zipcode;

    private String addr;

    private String addrDetail;

    private String tel;

    private String req;

    private int availablePoints;

    private int totalEarnedPoints;

    private int totalUsedPoints;

    private String oauth;

    /**
     * 생성자, 메소드 등
     */
    public static MemberDetailDTO createMemberDetailDTO(Member member, Address address) {
        return MemberDetailDTO.builder()
                .memberId(member.getId())
                .name(member.getName())
                .tel(member.getTel())
                .userIdentifier(member.getUserIdentifier())
                .email(member.getEmail())
                .zipcode(address.getZipcode())
                .addr(address.getAddr())
                .addrDetail(address.getAddrDetail())
                .req(address.getReq())
                .availablePoints(member.getAvailablePoints())
                .totalUsedPoints(member.getTotalUsedPoints())
                .totalEarnedPoints(member.getTotalUsedPoints())
                .oauth(member.getOauth())
                .build();
    }
}
