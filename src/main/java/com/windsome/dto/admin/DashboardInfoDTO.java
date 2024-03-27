package com.windsome.dto.admin;

import lombok.*;

import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class DashboardInfoDTO {

    private long totalMembers; // 전체 회원 수

    private long totalProducts; // 전체 상품 수

    private long totalQaPosts; // 전체 질문 게시물 수

    private Long totalOrderPrice; // 상품 총 판매액

    private List<CategorySalesDTO> categorySalesList; // 카테고리별 상품 판매 개수 리스트
}
