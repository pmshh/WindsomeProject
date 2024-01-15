package com.windsome.dto;

import com.windsome.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderItemDto {

    private Long itemId;

    private String itemNm;

    private int count;

    private int price;

    private String imgUrl;

    public OrderItemDto(OrderItem orderItem, String imgUrl) {
        this.itemId = orderItem.getItem().getId();
        this.itemNm = orderItem.getItem().getItemNm();
        this.count = orderItem.getCount();
        this.price = orderItem.getPrice();
        this.imgUrl = imgUrl;
    }
}
