package com.windsome.dto.order;

import com.windsome.entity.product.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderPageProductResponseDTO {

    private Long id; // 상품 id

    private int count; // 주문 개수

    private String imageUrl; // 상품 이미지 url

    private String name; // 상품명

    private int price; // 가격

    private double discount; // 할인율

    private int salePrice; // 할인 적용된 가격

    private int totalPrice; // 할인 적용된 가격 * 주문 수량

    private int point; // 상품 한 개 구매 시 획득 포인트

    private int totalPoint; // 상품 한 개 구매 시 획득 포인트 * 주문 수량

    public static OrderPageProductResponseDTO toDto(Product product, String imageUrl, int count) {
        return OrderPageProductResponseDTO.builder()
                .id(product.getId())
                .count(count)
                .imageUrl(imageUrl)
                .name(product.getName())
                .price(product.getPrice())
                .discount(product.getDiscount())
                .salePrice((int) Math.floor(product.getPrice() * (1 - product.getDiscount())))
                .totalPrice(((int) Math.floor(product.getPrice() * (1 - product.getDiscount()))) * count)
                .point((int) Math.floor((product.getPrice() * (1 - product.getDiscount())) * 0.05))
                .totalPoint(((int) Math.floor((product.getPrice() * (1 - product.getDiscount())) * 0.05)) * count)
                .build();
    }
}
