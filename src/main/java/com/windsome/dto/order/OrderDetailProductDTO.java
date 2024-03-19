package com.windsome.dto.order;

import com.windsome.constant.OrderProductStatus;
import com.windsome.dto.product.InventoryDTO;
import com.windsome.dto.product.ProductColorResponseDTO;
import com.windsome.dto.product.ProductSizeResponseDTO;
import com.windsome.entity.order.OrderProduct;
import com.windsome.entity.product.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    private Long colorId; // 색상 id

    private String colorName; // 색상 이름

    private Long sizeId; // 사이즈 id

    private String sizeName; // 사이즈 이름

    private int quantity; // 재고 수량

    private OrderProductStatus orderProductStatus; // 주문 상품 상태

    private int deliveryPrice = 2500; // 배송비


    public static OrderDetailProductDTO createDTO(OrderProduct orderProduct, String imageUrl, Inventory inventory) {
        OrderDetailProductDTO orderDetailProductDTO = new OrderDetailProductDTO();
        orderDetailProductDTO.setOrderProductId(orderProduct.getId());
        orderDetailProductDTO.setImageUrl(imageUrl);
        orderDetailProductDTO.setProductId(orderProduct.getProduct().getId());
        orderDetailProductDTO.setProductName(orderProduct.getProduct().getName());
        orderDetailProductDTO.setOrderQuantity(orderProduct.getOrderQuantity());
        orderDetailProductDTO.setProductPrice(orderProduct.getPrice());
        orderDetailProductDTO.setColorId(orderProduct.getColor().getId());
        orderDetailProductDTO.setColorName(orderProduct.getColor().getName());
        orderDetailProductDTO.setSizeId(orderProduct.getSize().getId());
        orderDetailProductDTO.setSizeName(orderProduct.getSize().getName());
        orderDetailProductDTO.setOrderProductStatus(orderProduct.getOrderProductStatus());
        if (orderProduct.getPrice() * orderProduct.getOrderQuantity() > 30000) {
            orderDetailProductDTO.setDeliveryPrice(0);
        }
        orderDetailProductDTO.setQuantity(inventory.getQuantity());
        return orderDetailProductDTO;
    }
}
