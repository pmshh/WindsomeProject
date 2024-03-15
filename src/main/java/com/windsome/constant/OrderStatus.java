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
    REFUNDED("환불완료"),
    INDIVIDUAL_PROCESSING("개별처리");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public static OrderStatus valueOfDisplayName(String displayName) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with displayName " + displayName + " found");
    }

    @Override
    public String toString() {
        return displayName;
    }

}
