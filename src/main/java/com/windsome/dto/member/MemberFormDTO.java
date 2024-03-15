package com.windsome.dto.member;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MemberFormDTO {

    private String userIdentifier;

    @NotBlank
    @Length(min = 1, max = 8)
    private String name;

    @NotBlank
    @Length(min = 8,max = 20)
    private String password;

    @NotBlank
    @Length(min = 8,max = 20)
    private String passwordConfirm;

    @Email
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
}
