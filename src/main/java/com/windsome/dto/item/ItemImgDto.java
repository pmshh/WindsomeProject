package com.windsome.dto.item;

import com.windsome.entity.ItemImg;
import lombok.Data;
import org.modelmapper.ModelMapper;

@Data
public class ItemImgDto {

    private Long id;

    private String imgName;

    private String oriImgName;

    private String imgUrl;

    private String repImgYn;

    /**
     * Entity -> Dto
     */
    private static ModelMapper modelMapper = new ModelMapper();

    public static ItemImgDto toDto(ItemImg itemImg) {
        return modelMapper.map(itemImg, ItemImgDto.class);
    }
}
