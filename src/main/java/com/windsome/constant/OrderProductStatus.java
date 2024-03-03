package com.windsome.constant;

import lombok.Getter;

@Getter
public enum OrderProductStatus {
    ORDER("주문완료"),
    CANCELED("주문취소"),
    RETURN_REQUESTED("반품요청"),
    RETURNED("반품완료"),
    EXCHANGE_REQUESTED("교환요청"),
    EXCHANGED("교환완료");

    private final String displayName;

    OrderProductStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
