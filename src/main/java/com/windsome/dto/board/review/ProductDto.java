package com.windsome.dto.board.review;

import com.windsome.entity.product.Product;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductDto {

    private Long productId;

    private String imageUrl;

    private String productName;

    private int price;

    private double discount;

    private int salePrice;

    // ToDo - model Mapper 적용
    public static ProductDto createProductDto(Product product, String imageUrl) {
        ProductDto productDto = new ProductDto();
        productDto.setProductId(product.getId());
        productDto.setProductName(product.getName());
        productDto.setImageUrl(imageUrl);
        productDto.setPrice(product.getPrice());
        productDto.setDiscount(product.getDiscount());
        productDto.setSalePrice((int) Math.floor(product.getPrice() * (1 - product.getDiscount())));
        return productDto;
    }
}
