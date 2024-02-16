package com.windsome.dto.account;

import com.querydsl.core.annotations.QueryProjection;
import com.windsome.constant.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class AccountInfoDto {

    private Long id;

    private String userIdentifier;

    private String email;

    private String name;

    private String address1;

    private String address2;

    private String address3;

    private Role state;

    private int point;

    private int totalPoint;

    private int totalUsePoint;

    private int totalOrderPrice;

    private LocalDateTime regTime;

    @QueryProjection
    public AccountInfoDto(Long id, String userIdentifier, String email, String name, String address1, String address2, String address3, Role state, int point, int totalPoint, int totalUsePoint, int totalOrderPrice, LocalDateTime regTime) {
        this.id = id;
        this.userIdentifier = userIdentifier;
        this.email = email;
        this.name = name;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.state = state;
        this.point = point;
        this.totalPoint = totalPoint;
        this.totalUsePoint = totalUsePoint;
        this.totalOrderPrice = totalOrderPrice;
        this.regTime = regTime;
    }
}
