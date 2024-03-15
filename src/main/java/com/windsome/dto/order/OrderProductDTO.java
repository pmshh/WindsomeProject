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
    private Long colorId; // 색상 id
    private String colorName; // 색상 이름
    private Long sizeId; // 사이즈 id
    private String sizeName; // 사이즈 이름
    private int orderQuantity; // 주문 수량
}
