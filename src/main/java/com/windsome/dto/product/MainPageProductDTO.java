package com.windsome.dto.product;

import com.querydsl.core.annotations.QueryProjection;
import com.windsome.entity.product.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class MainPageProductDTO {

    private Long id; // 상품 id

    private String productName;

    private Category category;

    private String productDetail;

    private String imageUrl;

    private Integer price;

    private double discount;

    private List<ProductOptionColorDTO> productOptionColors = new ArrayList<>();

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
