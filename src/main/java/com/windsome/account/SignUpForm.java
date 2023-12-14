package com.windsome.account;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SignUpForm {

    @NotBlank
    @Length(min = 5,max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{5,20}$")
    private String userId;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String nickname;

    @NotBlank
    @Length(min = 8,max = 20)
    private String password;

    private String address1;

    private String address2;

    private String address3;

}
