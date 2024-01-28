package com.windsome.dto.item;

import com.windsome.constant.ItemSellStatus;
import com.windsome.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemFormDto {

    private Long id;

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemNm;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    private double discount;

    @NotBlank(message = "내용은 필수 입력 값입니다.")
    private String itemDetail;

    @NotNull(message = "재고는 필수 입력 값입니다.")
    private Integer stockNumber;

    private ItemSellStatus itemSellStatus;

    private List<ItemImgDto> itemImgDtoList = new ArrayList<>();

    private List<Long> itemImgIds = new ArrayList<>();

    private Long categoryId;

    /**
     * Dto -> Entity, Entity -> Dto
     */
    private static ModelMapper modelMapper = new ModelMapper();

    public Item toEntity() {
        return modelMapper.map(this, Item.class);
    }

    public static ItemFormDto toDto(Item item) {
        return modelMapper.map(item, ItemFormDto.class);
    }
}
