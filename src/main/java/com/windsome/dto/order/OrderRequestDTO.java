package com.windsome.dto.order;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderRequestDTO {

    private String orderUid; // 주문 번호

    private String paymentUid; // 결제 고유 번호

    private String name; // 받는분 성함

    private int totalProductPrice; // 총 상품 금액

    private int totalOrderPrice; // 총 주문 금액 (총 상품 금액 + 배송비)

    private int totalPaymentPrice; // 총 결제 금액

    private String zipcode; // 우편 번호

    private String addr; // 받는분 주소

    private String addrDetail; // 상세 주소

    private String tel; // 받는분 전화 번호

    private String email; // 받는분 이메일

    private String req; // 배송 요청 사항

    private int earnedPoints; // 적립된 포인트 금액

    private int usedPoints; // 사용한 포인트 금액

    private int productCount; // 주문한 상품의 개수

    private String repProductName; // 대표 상품 이름

    private String repProductImage; // 대표 상품 이미지

    private List<OrderProductRequestDTO> orderProductDtoList; // 주문 상품 목록

}
