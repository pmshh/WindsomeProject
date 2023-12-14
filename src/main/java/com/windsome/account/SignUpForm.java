package com.windsome.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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
