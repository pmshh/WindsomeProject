package com.windsome.dto.order;

import com.windsome.constant.OrderProductStatus;
import com.windsome.entity.order.OrderProduct;
import com.windsome.entity.product.ProductOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailProductDTO {

    private Long orderProductId; // 주문 상품 id

    private String imageUrl; // 이미지 url

    private Long productId; // 상품 id

    private String productName; // 상품명

    private int orderQuantity; // 주문 수량

    private int productPrice; // 상품 금액

    private String color; // 색상

    private String size; // 사이즈

    private int quantity; // 재고 수량

    private OrderProductStatus orderProductStatus; // 주문 상품 상태

    private int deliveryPrice = 3000; // 배송비

    /**
     * 생성자, 메소드
     */
    public static OrderDetailProductDTO createDTO(OrderProduct orderProduct, String imageUrl, int inventory) {
        return OrderDetailProductDTO.builder()
                .orderProductId(orderProduct.getId())
                .imageUrl(imageUrl)
                .productId(orderProduct.getProduct().getId())
                .productName(orderProduct.getProduct().getName())
                .orderQuantity(orderProduct.getOrderQuantity())
                .productPrice(orderProduct.getPrice())
                .color("N/A")
                .size("N/A")
                .orderProductStatus(orderProduct.getOrderProductStatus())
                .deliveryPrice(orderProduct.getPrice() * orderProduct.getOrderQuantity() >= 30000 ? 0 : 3000)
                .quantity(inventory)
                .build();
    }

    public static OrderDetailProductDTO createDTO(OrderProduct orderProduct, String imageUrl, ProductOption productOption) {
        return OrderDetailProductDTO.builder()
                .orderProductId(orderProduct.getId())
                .imageUrl(imageUrl)
                .productId(orderProduct.getProduct().getId())
                .productName(orderProduct.getProduct().getName())
                .orderQuantity(orderProduct.getOrderQuantity())
                .productPrice(orderProduct.getPrice())
                .color(orderProduct.getColor())
                .size(orderProduct.getSize())
                .orderProductStatus(orderProduct.getOrderProductStatus())
                .deliveryPrice(orderProduct.getPrice() * orderProduct.getOrderQuantity() >= 30000 ? 0 : 3000)
                .quantity(productOption.getQuantity())
                .build();
    }
}
