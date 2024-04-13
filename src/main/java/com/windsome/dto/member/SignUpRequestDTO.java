package com.windsome.dto.member;

import com.windsome.entity.member.Address;
import com.windsome.entity.member.Member;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SignUpRequestDTO {

    @NotBlank
    @Length(min = 5,max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{5,20}$")
    private String userIdentifier;

    @NotBlank
    private String name;

    @NotBlank
    @Length(min = 8,max = 20)
    private String password;

    @NotBlank
    @Length(min = 8,max = 20)
    private String passwordConfirm;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String tel;

    @NotBlank
    private String zipcode;

    @NotBlank
    private String addr;

    private String addrDetail;

    public Address toAddress(Member member, SignUpRequestDTO signUpRequestDTO) {
        Address address = Address.builder()
                .member(member)
                .name(signUpRequestDTO.getName())
                .zipcode(signUpRequestDTO.getZipcode())
                .addr(signUpRequestDTO.getAddr())
                .addrDetail(signUpRequestDTO.getAddrDetail())
                .tel(signUpRequestDTO.getTel())
                .req("")
                .isDefault(true)
                .build();
        address.setMember(member);
        return address;
    }
}
