package com.windsome.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductColorResponseDTO {

    private Long productColorId;
    private Long productId;
    private Long colorId;
    private String name;
}
