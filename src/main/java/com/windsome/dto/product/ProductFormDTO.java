package com.windsome.dto.product;

import com.windsome.constant.ProductSellStatus;
import com.windsome.entity.product.Product;
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
public class ProductFormDTO {

    private Long id;

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String name;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    private double discount;

    @NotBlank(message = "내용은 필수 입력 값입니다.")
    private String productDetail;

    private int inventory; // 재고

    private ProductSellStatus productSellStatus; // 상품 판매 상태

    private List<ProductImageDTO> productImageDTOList = new ArrayList<>(); // 상품 이미지 리스트

    private List<Long> productImageIds = new ArrayList<>(); // 상품 이미지 Ids

    private Long categoryId; // 카테고리 id

    private List<OptionDTO> optionDTOList = new ArrayList<>(); // 상품 옵션(색상, 사이즈, 재고)

    /**
     * 생성자
     */
    private static ModelMapper modelMapper = new ModelMapper();

    public static ProductFormDTO toDto(Product item) {
        return modelMapper.map(item, ProductFormDTO.class);
    }
}
