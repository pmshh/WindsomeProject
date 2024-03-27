package com.windsome.dto.cart;

import lombok.*;

@Getter @Setter
@ToString
@NoArgsConstructor
public class CartDetailDTO {

    private Long cartProductId; // cart에 담긴 상품 id

    private Long productId; // 상품 id

    private String productName; // 상품명

    private double discount; // 할인율

    private String color; // 색상

    private String size; // 사이즈

    private int stockQuantity; // 재고 수량

    private int orderQuantity; // 주문 수량

    private int price; // 상품 가격

    private int salePrice; // 할인 적용된 가격

    private int totalPrice; // 상품 총 가격

    private String imageUrl; // 상품 대표 이미지 url

    public CartDetailDTO(Long cartProductId, Long productId, String productName, double discount, String color, String size, int stockQuantity, int orderQuantity, int price, String imageUrl) {
        this.cartProductId = cartProductId;
        this.productId = productId;
        this.productName = productName;
        this.discount = discount;
        this.color = color;
        this.size = size;
        this.stockQuantity = stockQuantity;
        this.orderQuantity = orderQuantity;
        this.price = price;
        this.salePrice = (int) Math.floor(price * (1 - discount));
        this.totalPrice = salePrice * orderQuantity;
        this.imageUrl = imageUrl;
    }
}
