package com.windsome.dto.order;

import com.windsome.entity.Product;
import com.windsome.entity.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class OrderPageProductDto {

    private Long productId;

    private int count;

    private String imageUrl;

    private String productName;

    private int price;

    private double discount;

    private int salePrice; // 할인 적용된 가격
    private int totalPrice; // 할인 적용된 가격 * 주문 수량
    private int point; // 상품 한 개 구매 시 획득 포인트
    private int totalPoint; // 상품 한 개 구매 시 획득 포인트 * 주문 수량

    public static OrderPageProductDto toDto(Product product, ProductImage productImage, OrderPageProductDto orderDto) {
        OrderPageProductDto orderPageProductDto = new OrderPageProductDto();
        orderPageProductDto.setProductId(product.getId());
        orderPageProductDto.setCount(orderDto.getCount());
        orderPageProductDto.setImageUrl(productImage.getImageUrl());
        orderPageProductDto.setProductName(product.getName());
        orderPageProductDto.setPrice(product.getPrice());
        orderPageProductDto.setDiscount(product.getDiscount());
        return orderPageProductDto;
    }

    public void initPriceInfo() {
        this.salePrice = (int) (this.price * (1 - this.discount));
        this.totalPrice = this.salePrice * this.count;
        this.point = (int)(Math.floor(this.salePrice*0.05));
        this.totalPoint = this.point * this.count;
    }
}
