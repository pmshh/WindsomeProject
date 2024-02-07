package com.windsome.dto.board.review;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemListDto {

    private String imgUrl;
    private Long itemId;
    private String itemNm;
    private int price;
    private double discount;
    private int salePrice;

    public ItemListDto(String imgUrl, Long itemId, String itemNm, int price, double discount) {
        this.imgUrl = imgUrl;
        this.itemId = itemId;
        this.itemNm = itemNm;
        this.price = price;
        this.discount = discount;
        this.salePrice = (int) Math.floor(price * (1 - discount));
    }
}
