package com.windsome.constant;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PROCESSING("배송준비중"),
    SHIPPED("배송중"),
    DELIVERED("배송완료"),
    CANCELED("주문취소"),
    RETURN_REQUESTED("반품요청"),
    RETURNED("반품완료"),
    EXCHANGE_REQUESTED("교환요청"),
    EXCHANGED("교환완료"),
    REFUND_REQUESTED("환불요청"),
    REFUNDED("환불완료");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
