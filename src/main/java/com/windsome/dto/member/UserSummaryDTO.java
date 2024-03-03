package com.windsome.dto.member;

import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
public class UserSummaryDTO {

    private Long memberId; // 회원 기본키

    private String name; // 회원 이름

    private int availablePoints; // 사용 가능한 포인트

    private int totalEarnedPoints; // 총 적립 포인트

    private int totalUsedPoints; // 총 사용 포인트

    public UserSummaryDTO(Long memberId, String name, int availablePoints, int totalEarnedPoints, int totalUsedPoints) {
        this.memberId = memberId;
        this.name = name;
        this.availablePoints = availablePoints;
        this.totalEarnedPoints = totalEarnedPoints;
        this.totalUsedPoints = totalUsedPoints;
    }
}
