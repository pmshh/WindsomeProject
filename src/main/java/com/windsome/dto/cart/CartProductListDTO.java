package com.windsome.dto.cart;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartProductListDTO {

    @NotNull(message = "상품 아이디는 필수 입력 값 입니다.")
    private Long productId;

    private List<CartProductDTO> cartProductDTOList = new ArrayList<>();
}
