package com.windsome.dto.board.review;

import com.windsome.entity.Item;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemDto {

    private Long itemId;

    private String imgUrl;

    private String itemNm;

    private int price;

    private double discount;

    private int salePrice;

    public static ItemDto createReviewEnrollPageItemDto(Item item, String imgUrl) {
        ItemDto itemDto = new ItemDto();
        itemDto.setItemNm(item.getItemNm());
        itemDto.setImgUrl(imgUrl);
        itemDto.setItemId(item.getId());
        itemDto.setPrice(item.getPrice());
        itemDto.setDiscount(item.getDiscount());
        itemDto.setSalePrice((int) Math.floor(item.getPrice() * (1 - item.getDiscount())));
        return itemDto;
    }
}
