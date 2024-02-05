package com.windsome.dto.account;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdatePasswordDto {

    private String userIdentifier;
    private String name;
    private String email;
    private String password;
}
