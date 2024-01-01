package com.windsome.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CartDetailDto {

    private Long cartItemId;

    private Long itemId;

    private String itemNm;

    private int price;

    private int stockNumber;

    private int count;

    private String imgUrl;

    public CartDetailDto(Long cartItemId, Long itemId, String itemNm, int price, int stockNumber, int count, String imgUrl) {
        this.cartItemId = cartItemId;
        this.itemId = itemId;
        this.itemNm = itemNm;
        this.price = price;
        this.stockNumber = stockNumber;
        this.count = count;
        this.imgUrl = imgUrl;
    }
}
