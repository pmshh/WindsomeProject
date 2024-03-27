package com.windsome.dto.board.review;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ProductListDTO {

    private String imageUrl;

    private Long productId;

    private String productName;

    private int price;

    private double discount;

    private int salePrice;

    public ProductListDTO(String imageUrl, Long productId, String productName, int price, double discount) {
        this.imageUrl = imageUrl;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.discount = discount;
        this.salePrice = (int) Math.floor(price * (1 - discount));
    }
}
