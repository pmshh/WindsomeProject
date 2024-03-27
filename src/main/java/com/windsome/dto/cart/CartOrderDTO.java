package com.windsome.dto.cart;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartOrderDTO {

    private List<Long> cartProductIds;

}
