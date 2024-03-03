package com.windsome.dto.order;

import com.windsome.constant.OrderProductStatus;
import com.windsome.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDTO {

    /**
     * 주문 정보
     */
    private String orderUid; // 주문 번호

    private LocalDateTime orderDate; // 주문 날짜

    private String buyer; // 주문자 이름

    private OrderStatus orderStatus; // 주문 처리 상태

    /**
     * 결제 정보
     */
    private int totalOrderPrice; // 총 주문 금액

    private int totalPaymentPrice; // 총 결제 금액

    private String paymentMethod; // 결제 수단

    private int usedPoints; // 사용한 포인트

    private int earnedPoints; // 적립 포인트

    /**
     * 주문 상품 정보
     */
    List<OrderDetailProductDTO> orderDetailProductList = new ArrayList<>();

    /**
     * 배송지 정보
     */
    private String recipient; // 받는 사람

    private String zipcode; // 우편 번호

    private String address; // 주소

    private String tel; // 휴대 전화

    private String req; // 배송 메시지

    public OrderDetailDTO(String orderUid, LocalDateTime orderDate, String buyer, OrderStatus orderStatus, int totalOrderPrice, int totalPaymentPrice, int usedPoints, int earnedPoints, String recipient, String zipcode, String addr, String addrDetail, String tel, String req) {
        this.orderUid = orderUid;
        this.orderDate = orderDate;
        this.buyer = buyer;
        this.orderStatus = orderStatus;
        this.totalOrderPrice = totalOrderPrice;
        this.totalPaymentPrice = totalPaymentPrice;
        this.paymentMethod = "card";
        this.usedPoints = usedPoints;
        this.earnedPoints = earnedPoints;
        this.recipient = recipient;
        this.zipcode = zipcode;
        this.address = addr + addrDetail;
        this.tel = tel;
        this.req = req;
    }
}
