package com.windsome.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class ProfileDto {

    private String userIdentifier;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Length(min = 1, max = 8)
    private String name;

    @NotBlank
    @Length(min = 8,max = 20)
    private String password;

    @NotBlank
    @Length(min = 8,max = 20)
    private String passwordConfirm;

    private String address1;

    private String address2;

    private String address3;

    public ProfileDto() {
    }

}
