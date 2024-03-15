package com.windsome.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptionDTO {

    private Long sizeId; // 사이즈 id
    private Long colorId; // 컬러 id
    private int quantity; // 재고
}
