package com.windsome.dto.order;

import com.windsome.constant.OrderProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailProductDTO {

    private String imageUrl; // 이미지 url

    private Long productId; // 상품 id

    private String productName; // 상품명

    private int orderQuantity; // 주문 수량

    private int productPrice; // 상품 금액

    private OrderProductStatus orderProductStatus; // 배송 상태

    private int deliveryPrice = 2500; // 배송비

}
