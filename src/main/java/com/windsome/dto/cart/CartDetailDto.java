package com.windsome.dto.cart;

import lombok.*;

@Getter @Setter
@ToString
@NoArgsConstructor
public class CartDetailDto {

    private Long cartProductId; // cart에 담긴 상품 id

    private Long productId; // 상품 id

    private String productName;

    private int stockNumber;

    private double discount;

    private int count;

    private int price;

    private int salePrice;

    private int totalPrice;

    private int point;

    private int totalPoint;

    private String imageUrl;

    public CartDetailDto(Long cartProductId, Long productId, String productName, int stockNumber, double discount, int count, int price, String imageUrl) {
        this.cartProductId = cartProductId;
        this.productId = productId;
        this.productName = productName;
        this.stockNumber = stockNumber;
        this.discount = discount;
        this.count = count;
        this.price = price;
        this.salePrice = (int) Math.floor(price * (1 - discount));
        this.totalPrice = salePrice * count;
        this.point = (int) Math.floor(salePrice * 0.05);
        this.totalPoint = point * count;
        this.imageUrl = imageUrl;
    }
}
