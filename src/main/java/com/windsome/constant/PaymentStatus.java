package com.windsome.constant;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PAYMENT_COMPLETED("결제완료"),
    PAYMENT_CANCELLED("결제취소"),
    REFUND_REQUESTED("환불요청"),
    REFUND_COMPLETED("환불완료");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
