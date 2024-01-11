package com.windsome.dto;

import com.windsome.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class ItemSearchDto {

    private String page;

    private String searchDateType;

    private ItemSellStatus searchSellStatus;

    private String searchBy;

    private String searchQuery = "";

    private Long category;
}
