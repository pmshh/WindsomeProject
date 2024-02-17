package com.windsome.dto.account;

import com.windsome.entity.Account;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class AdminPageProfileFormDto {

    private Long id;

    private String userIdentifier;

    private String password;

    private String name;

    private String email;

    private String address1;

    private String address2;

    private String address3;

    private int point;

    private int totalPoint;

    private int totalUsePoint;

    private int totalOrderPrice;

    public static AdminPageProfileFormDto toDto(Account account) {
        return AdminPageProfileFormDto.builder()
                .id(account.getId())
                .userIdentifier(account.getUserIdentifier())
                .email(account.getEmail())
                .name(account.getName())
                .password(account.getPassword())
                .address1(account.getAddress1())
                .address2(account.getAddress2())
                .address3(account.getAddress3())
                .point(account.getPoint())
                .totalPoint(account.getTotalPoint())
                .totalUsePoint(account.getTotalUsePoint())
                .totalOrderPrice(account.getTotalOrderPrice())
                .build();
    }
}
