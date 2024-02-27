package com.windsome.dto.admin;

import com.windsome.constant.ProductSellStatus;
import lombok.Data;

@Data
public class PageDto {

    private String page;

    private String searchDateType;

    private ProductSellStatus searchSellStatus;

    private String searchBy;

    private String searchQuery = "";
}
