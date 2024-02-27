package com.windsome.dto.product;

import com.windsome.constant.ProductSellStatus;
import com.windsome.entity.Product;
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
public class ProductFormDto {

    private Long id;

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String productName;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    private double discount;

    @NotBlank(message = "내용은 필수 입력 값입니다.")
    private String productDetail;

    @NotNull(message = "재고는 필수 입력 값입니다.")
    private Integer stockNumber;

    private ProductSellStatus productSellStatus;

    private List<ProductImageDto> productImageDtoList = new ArrayList<>();

    private List<Long> productImageIds = new ArrayList<>();

    private Long categoryId;

    /**
     * Dto -> Entity, Entity -> Dto
     */
    private static ModelMapper modelMapper = new ModelMapper();

    public Product toEntity() {
        return modelMapper.map(this, Product.class);
    }

    public static ProductFormDto toDto(Product item) {
        return modelMapper.map(item, ProductFormDto.class);
    }
}
