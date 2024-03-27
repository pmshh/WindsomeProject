package com.windsome.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPageOrderProductDTO {

    private Long orderProductId; // 주문 상품 id

    private String color; // 색상

    private String size; // 사이즈

    private int orderQuantity; // 주문 개수

    private String orderProductStatus; // 주문 상품 상태

}
