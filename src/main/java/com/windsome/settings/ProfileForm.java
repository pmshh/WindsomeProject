package com.windsome.settings;

import com.windsome.domain.Account;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class ProfileForm {

    private String userId;

    @Email
    private String email;

    @NotBlank
    @Length(min = 3,max = 16)
    private String nickname;

    @NotBlank
    @Length(min = 8,max = 20)
    private String newPassword;

    @NotBlank
    @Length(min = 8,max = 20)
    private String newPasswordConfirm;

    private String address1;

    private String address2;

    private String address3;

    public ProfileForm() {
    }

    public ProfileForm(Account account) {
        this.userId = account.getUserId();
        this.email = account.getEmail();
        this.nickname = account.getNickname();
        this.address1 = account.getAddress1();
        this.address2 = account.getAddress2();
        this.address3 = account.getAddress3();
    }
}
