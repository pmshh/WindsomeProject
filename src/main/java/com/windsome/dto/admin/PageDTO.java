package com.windsome.dto.admin;

import com.windsome.constant.ProductSellStatus;
import lombok.Data;

@Data
public class PageDTO {

    private String page = "0";

    private String searchDateType = "";

    private ProductSellStatus searchSellStatus;

    private String searchBy = "";

    private String searchQuery = "";
}
