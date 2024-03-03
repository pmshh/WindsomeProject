package com.windsome.dto.product;

import com.windsome.entity.ProductImage;
import lombok.Data;
import org.modelmapper.ModelMapper;

@Data
public class ProductImageDTO {

    private Long id;

    private String serverImageName;

    private String originalImageName;

    private String imageUrl;

    private String isRepresentativeImage;

    /**
     * Entity -> Dto
     */
    private static ModelMapper modelMapper = new ModelMapper();

    public static ProductImageDTO toDto(ProductImage productImage) {
        return modelMapper.map(productImage, ProductImageDTO.class);
    }
}
