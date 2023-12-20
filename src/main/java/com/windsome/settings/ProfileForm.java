package com.windsome.settings;

import com.windsome.domain.Account;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class ProfileForm {

    @Email
    private String email;

    @NotBlank
    @Length(min = 3,max = 16)
    private String nickname;

    @NotBlank
    @Length(min = 8,max = 20)
    private String password;

    @NotBlank
    @Length(min = 8,max = 20)
    private String passwordConfirm;

    private String address1;

    private String address2;

    private String address3;

    public ProfileForm() {
    }

}
