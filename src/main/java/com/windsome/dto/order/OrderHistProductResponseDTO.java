package com.windsome.dto.order;

import com.windsome.constant.OrderProductStatus;
import com.windsome.dto.product.ProductInfoResponseDTO;
import com.windsome.entity.OrderProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistProductResponseDTO {

    private Long id; // 상품 id

    private String name; // 상품명

    private int price; // 가격

    private int count; // 개수

    private OrderProductStatus orderProductStatus; // 주문 상품 현황

    private String imageUrl; // 대표 이미지 url

    public OrderHistProductResponseDTO(OrderProduct orderProduct, ProductInfoResponseDTO productInfoDto) {
        this.id = orderProduct.getProduct().getId();
        this.name = productInfoDto.getName();
        this.price = orderProduct.getPrice();
        this.count = orderProduct.getCount();
        this.orderProductStatus = orderProduct.getOrderProductStatus();
        this.imageUrl = productInfoDto.getImageUrl();
    }
}
