package com.windsome.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrderDto {

    private String address1;

    private String address2;

    private String address3;

    private String tel;

    private String email;

    private String req; // 배송 메시지

    private List<OrderItemDto> orders;

    private int deliveryCost;

    private int usePoint;

    /**
     * DB에 존재 하지 않는 데이터
     */
    private int orderSalePrice; // 주문 가격
    private int orderSavePoint; // 적립 포인트
    private int orderFinalSalePrice; // 최종 판매 비용

    /**
     * 가격 관련 정보 초기화
     */
    public void initOrderPriceInfo() {
        for(OrderItemDto order : orders) {
            orderSalePrice += order.getTotalSalePrice();
            orderSavePoint += order.getTotalSavePoint();
        }

        if (orderSalePrice >= 30000) {
            deliveryCost = 0;
        } else {
            deliveryCost = 2500;
        }

        orderFinalSalePrice = orderSalePrice + deliveryCost - usePoint;
    }
}
