package com.windsome.dto;

import com.windsome.constant.ItemSellStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemDto {

    private Long id;

    private String itemNm; // 상품명

    private int price; // 가격

    private int stockNumber; // 재고 수량

    private String itemDetail; // 상품 상세 설명

    private ItemSellStatus itemSellStatus; // 상품 판매 상태

    private double discount; // 할인율

//    private Category cateCode; // 카테고리

    private LocalDateTime regTime; // 등록 시간

    private LocalDateTime updateTime; // 수정 시간
}
