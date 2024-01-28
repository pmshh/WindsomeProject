package com.windsome.dto.order;

import com.windsome.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrderItemDto {

    private Long itemId;

    private String itemNm;

    private int price;

    private int count;

    private double discount;

    private int savePoint;

    private String imgUrl;

    /**
     * DB에 존재 하지 않는 데이터
     */
    private int salePrice; // 할인 적용된 가격
    private int totalSalePrice; // 할인 적용된 가격 * 주문 수량
    private int totalSavePoint; // 상품 한 개 구매 시 획득 포인트 * 주문 수량

    /**
     * 가격 관련 정보 초기화
     */
    public void initPriceAndPoint() {
        this.salePrice = (int) (this.price * (1 - this.discount));
        this.totalSalePrice = this.salePrice * this.count;
        this.savePoint = (int) (Math.floor(this.salePrice * 0.05));
        this.totalSavePoint = this.savePoint * this.count;
    }

    /**
     * 주문 조회 할 때 필요한 생성자
     */
    public OrderItemDto(OrderItem orderItem, String imgUrl) {
        this.itemId = orderItem.getItem().getId();
        this.itemNm = orderItem.getItem().getItemNm();
        this.count = orderItem.getCount();
        this.price = orderItem.getPrice();
        this.discount = orderItem.getDiscount();
        this.imgUrl = imgUrl;
    }
}
