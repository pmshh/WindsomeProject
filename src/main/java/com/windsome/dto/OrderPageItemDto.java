package com.windsome.dto;

import com.windsome.entity.Item;
import com.windsome.entity.ItemImg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class OrderPageItemDto {

    private Long itemId;

    private int count;

    private String imgUrl;

    private String itemNm;

    private int price;

    private double discount;

    private int salePrice; // 할인 적용된 가격
    private int totalPrice; // 할인 적용된 가격 * 주문 수량
    private int point; // 상품 한 개 구매 시 획득 포인트
    private int totalPoint; // 상품 한 개 구매 시 획득 포인트 * 주문 수량

    public static OrderPageItemDto createOrderPageItemDto(Item item, ItemImg itemImg, OrderPageItemDto orderDto) {
        OrderPageItemDto orderPageItemDto = new OrderPageItemDto();
        orderPageItemDto.setItemId(item.getId());
        orderPageItemDto.setCount(orderDto.getCount());
        orderPageItemDto.setImgUrl(itemImg.getImgUrl());
        orderPageItemDto.setItemNm(item.getItemNm());
        orderPageItemDto.setPrice(item.getPrice());
        orderPageItemDto.setDiscount(item.getDiscount());
        return orderPageItemDto;
    }

    public void initPriceInfo() {
        this.salePrice = (int) (this.price * (1 - this.discount));
        this.totalPrice = this.salePrice * this.count;
        this.point = (int)(Math.floor(this.salePrice*0.05));
        this.totalPoint = this.point * this.count;
    }
}
