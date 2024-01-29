package com.windsome.dto.cart;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class CartDetailDto {

    private Long cartItemId;

    private Long itemId;

    private String itemNm;

    private int stockNumber;

    private double discount;

    private int count;

    private int price;

    private int salePrice;

    private int totalPrice;

    private int point;

    private int totalPoint;

    private String imgUrl;

    public CartDetailDto(Long cartItemId, Long itemId, String itemNm, int stockNumber, double discount, int count, int price, String imgUrl) {
        this.cartItemId = cartItemId;
        this.itemId = itemId;
        this.itemNm = itemNm;
        this.stockNumber = stockNumber;
        this.discount = discount;
        this.count = count;
        this.price = price;
        this.salePrice = (int) Math.floor(price * (1 - discount));
        this.totalPrice = salePrice * count;
        this.point = (int) Math.floor(salePrice * 0.05);
        this.totalPoint = point * count;
        this.imgUrl = imgUrl;
    }
}