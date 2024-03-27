package com.windsome.dto.cart;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartProductDTO {
    private String color; // 색상

    private String size; // 사이즈

    @Min(value = 1, message = "최소 1개 이상 담아주세요.")
    private int quantity; // 주문 수량
}
