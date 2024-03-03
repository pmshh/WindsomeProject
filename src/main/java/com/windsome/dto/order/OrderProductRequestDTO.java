package com.windsome.dto.order;

import com.windsome.constant.OrderProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrderProductRequestDTO {

    private Long id; // 상품 id

    private int price; // 가격

    private int count; // 개수

    private OrderProductStatus orderProductStatus; // 주문 상품 현황
}
