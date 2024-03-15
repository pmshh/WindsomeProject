package com.windsome.constant;

import lombok.Getter;

@Getter
public enum OrderProductStatus {
    ORDER("주문완료"),
    PREPARING_FOR_SHIPPING("배송준비중"),
    SHIPPING("배송중"),
    DELIVERED("배송완료"),
    DELIVERY_FAILED("배송실패"),
    DELIVERY_DELAYED("배송지연"),
    CANCELED("주문취소"),
    RETURN_REQUESTED("반품요청"),
    RETURNED("반품완료"),
    EXCHANGE_REQUESTED("교환요청"),
    EXCHANGED("교환완료");

    private final String displayName;

    OrderProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public static OrderProductStatus valueOfDisplayName(String displayName) {
        for (OrderProductStatus status : OrderProductStatus.values()) {
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
