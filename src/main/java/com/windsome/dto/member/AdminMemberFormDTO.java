package com.windsome.dto.member;

import com.windsome.entity.member.Address;
import com.windsome.entity.member.Member;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMemberFormDTO {

    @NotBlank
    private String userIdentifier;

    @NotBlank
    private String name;

    @NotBlank
    private String password;

    @NotBlank
    private String email;

    @NotBlank
    private String tel;

    @NotBlank
    private String zipcode;

    @NotBlank
    private String addr;

    @NotBlank
    private String addrDetail;

    private int availablePoints = 0;

    private int totalEarnedPoints = 0;

    private int totalUsedPoints = 0;

    public Address toAddress(Member member, AdminMemberFormDTO memberFormDTO) {
        Address address = Address.builder()
                .member(member)
                .name(memberFormDTO.getName())
                .zipcode(memberFormDTO.getZipcode())
                .addr(memberFormDTO.getAddr())
                .addrDetail(memberFormDTO.getAddrDetail())
                .tel(memberFormDTO.getTel())
                .req("")
                .isDefault(true)
                .build();
        address.setMember(member);
        return address;
    }
}
