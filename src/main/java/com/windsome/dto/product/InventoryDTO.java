package com.windsome.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO {

    private Long productId; // 상품 id
    private Long sizeId; // 사이즈 id
    private Long colorId; // 색상 id
    private String sizeName; // 사이즈 이름
    private String colorName; // 색상 이름
    private String colorCode; // 색상 코드
    private int quantity; // 재고
}
