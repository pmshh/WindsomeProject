package com.windsome.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderProductListDTO {
    private List<OrderProductDTO> orderProducts; // 주문 상품 정보
}
