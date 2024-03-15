package com.windsome.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPageOrderDTO {
    /**
     * 주문 정보
     */
    private String orderStatus; // 주문 처리 상태

    /**
     * 결제 정보
     */
    private int totalOrderPrice; // 총 주문 금액

    private int totalPaymentPrice; // 총 결제 금액

    private int usedPoints; // 사용한 포인트

    private int earnedPoints; // 적립 포인트

    /**
     * 주문 상품 정보
     */
    private List<AdminPageOrderProductDTO> orderProducts = new ArrayList<>();

    /**
     * 배송지 정보
     */
    private String recipient; // 받는 사람

    private String zipcode; // 우편 번호

    private String addr; // 주소

    private String addrDetail; // 상세 주소

    private String tel; // 휴대 전화

    private String req; // 배송 메시지
}
