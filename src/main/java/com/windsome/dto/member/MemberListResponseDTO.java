package com.windsome.dto.member;

import com.querydsl.core.annotations.QueryProjection;
import com.windsome.constant.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class MemberListResponseDTO {

    private Long id;

    private String userIdentifier;

    private String email;

    private String name;

    private String zipcode;

    private String addr;

    private String addrDetail;

    private Role role;

    private int availablePoints;

    private int totalEarnedPoints;

    private int totalUsedPoints;

    private LocalDateTime regTime;

    @QueryProjection
    public MemberListResponseDTO(Long id, String userIdentifier, String email, String name, String zipcode, String addr, String addrDetail, Role role, int availablePoints, int totalEarnedPoints, int totalUsedPoints, LocalDateTime regTime) {
        this.id = id;
        this.userIdentifier = userIdentifier;
        this.email = email;
        this.name = name;
        this.zipcode = zipcode;
        this.addr = addr;
        this.addrDetail = addrDetail;
        this.role = role;
        this.availablePoints = availablePoints;
        this.totalEarnedPoints = totalEarnedPoints;
        this.totalUsedPoints = totalUsedPoints;
        this.regTime = regTime;
    }
}
