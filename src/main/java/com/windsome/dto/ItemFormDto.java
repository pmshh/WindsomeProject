package com.windsome.dto;

import com.windsome.constant.ItemSellStatus;
import com.windsome.entity.Item;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.ui.Model;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
public class ItemFormDto {

    private Long id;

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemNm;

    @NotBlank(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    @NotBlank(message = "내용은 필수 입력 값입니다.")
    private String itemDetail;

    @NotBlank(message = "재고는 필수 입력 값입니다.")
    private Integer stockNumber;

    private ItemSellStatus itemSellStatus;

    private List<ItemImgDto> itemImgDtoList = new ArrayList<>();

    private List<Long> itemImgIds = new ArrayList<>();

    private static ModelMapper modelMapper = new ModelMapper();

    public Item createItem() {
        return modelMapper.map(this, Item.class);
    }

    public static ItemFormDto of(Item item) {
        return modelMapper.map(item, ItemFormDto.class);
    }
}
