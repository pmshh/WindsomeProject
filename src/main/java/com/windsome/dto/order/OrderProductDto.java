package com.windsome.dto.order;

import com.windsome.constant.OrderProductStatus;
import com.windsome.entity.OrderProduct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrderProductDto {

    private Long productId;

    private String productName;

    private int price;

    private int count;

    private double discount;

    private int accumulatedPoints;

    private String imageUrl;

    private OrderProductStatus orderProductStatus;

    /**
     * DB에 존재 하지 않는 데이터
     */
    private int salePrice; // 할인 적용된 가격

    private int totalSalePrice; // 할인 적용된 가격 * 주문 수량

    private int totalSavePoint; // 상품 한 개 구매 시 획득 포인트 * 주문 수량

    /**
     * 주문 조회 할 때 필요한 생성자
     */
    public OrderProductDto(OrderProduct orderProduct, String imageUrl) {
        this.productId = orderProduct.getProduct().getId();
        this.productName = orderProduct.getProduct().getName();
        this.count = orderProduct.getCount();
        this.price = orderProduct.getPrice();
        this.discount = orderProduct.getDiscount();
        this.orderProductStatus = orderProduct.getOrderProductStatus();
        this.imageUrl = imageUrl;
    }
}
