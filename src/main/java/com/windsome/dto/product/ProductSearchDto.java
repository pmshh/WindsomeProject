package com.windsome.dto.product;

import com.windsome.constant.ProductSellStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductSearchDTO {

    private String page;

    private String sort = "";

    private String searchDateType;

    private ProductSellStatus searchSellStatus;

    private String searchBy;

    private String searchQuery = "";

    private Long category;
}
