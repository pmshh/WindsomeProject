package com.windsome.dto.member;

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
}
