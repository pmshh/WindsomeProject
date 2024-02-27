package com.windsome.dto.member;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePasswordDto {

    private String userIdentifier;

    private String name;

    private String email;

    private String password;

    public UpdatePasswordDto(String userIdentifier, String name, String email) {
        this.userIdentifier = userIdentifier;
        this.name = name;
        this.email = email;
    }
}
