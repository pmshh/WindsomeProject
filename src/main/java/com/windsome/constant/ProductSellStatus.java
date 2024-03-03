package com.windsome.constant;

import lombok.Getter;

@Getter
public enum ProductSellStatus {
    AVAILABLE("판매가능"),
    SOLD_OUT("품절"),
    DISCONTINUED("단종");

    private final String displayName;

    ProductSellStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
