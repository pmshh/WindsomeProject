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
    @NotNull(message = "색상 아이디는 필수 입력 값 입니다.")
    private Long colorId; // 색상 id

    @NotNull(message = "사이즈 아이디는 필수 입력 값 입니다.")
    private Long sizeId; // 사이즈 id

    @Min(value = 1, message = "최소 1개 이상 담아주세요.")
    private int quantity; // 주문 수량
}
