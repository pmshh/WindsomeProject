package com.windsome.dto.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ProductInfoResponseDTO {

    private String name;
    private String imageUrl;

    public ProductInfoResponseDTO(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
