package com.windsome.dto.order;

import com.windsome.entity.cart.CartProduct;
import com.windsome.entity.product.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderProductResponseDTO {

    private Long id; // 상품 id

    private Long cartProductId; // 장바구니 상품 id

    private String imageUrl; // 상품 이미지 url

    private String name; // 상품명

    private int price; // 가격

    private double discount; // 할인율

    private String color; // 색상

    private String size; // 사이즈

    private int orderQuantity; // 주문 수량

    public static OrderProductResponseDTO createDTO(OrderProductDTO orderProductDTO, Product product, String imageUrl, CartProduct cartProduct) {
        return OrderProductResponseDTO.builder()
                .id(product.getId())
                .price(product.getPrice())
                .discount(product.getDiscount())
                .name(product.getName())
                .imageUrl(imageUrl)
                .color(orderProductDTO.getColor())
                .size(orderProductDTO.getSize())
                .orderQuantity(orderProductDTO.getOrderQuantity())
                .cartProductId(cartProduct != null ? cartProduct.getId() : null)
                .build();
    }
}
