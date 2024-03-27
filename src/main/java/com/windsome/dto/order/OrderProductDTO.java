package com.windsome.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderProductDTO {

    private Long productId; // 상품 id

    private String color; // 색상

    private String size; // 사이즈

    private int orderQuantity; // 주문 수량

}
