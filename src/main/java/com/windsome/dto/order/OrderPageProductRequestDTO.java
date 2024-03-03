package com.windsome.dto.order;

import lombok.Data;

@Data
public class OrderPageProductRequestDTO {

    private Long productId; // 상품 id
    private int count; // 주문 개수
}
