package com.windsome.dto.product;

import com.querydsl.core.annotations.QueryProjection;
import com.windsome.entity.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class MainPageProductDTO {

    private Long id;

    private String productName;

    private Category category;

    private String productDetail;

    private String imageUrl;

    private Integer price;

    private double discount;

    @QueryProjection
    public MainPageProductDTO(Long id, String productName, Category category, String productDetail, String imageUrl, Integer price, double discount) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.productDetail = productDetail;
        this.imageUrl = imageUrl;
        this.price = price;
        this.discount = discount;
    }
}
