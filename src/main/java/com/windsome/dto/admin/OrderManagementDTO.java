package com.windsome.dto.admin;

import com.windsome.constant.OrderStatus;
import com.windsome.dto.order.OrderHistProductResponseDTO;
import com.windsome.entity.order.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class OrderManagementDTO {

    private Long orderId; // 주문 id

    private String orderUid; // 주문 번호

    private String orderDate; // 주문 날짜

    private OrderStatus orderStatus; // 주문 현황

    private String buyerId; // 구매자 아이디

    private int productCount; // 주문 상품 개수

    private String repProductImage; // 대표 이미지

    private String repProductName; // 대표 상품명

    private List<OrderHistProductResponseDTO> orderHistProductList = new ArrayList<>(); // 주문 품목

    public OrderManagementDTO(Order order) {
        this.orderId = order.getId();
        this.orderUid = order.getOrderUid();
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.orderStatus = order.getOrderStatus();
        this.buyerId = order.getMember().getUserIdentifier();
        this.productCount = order.getProductCount();
        this.repProductImage = order.getRepProductImage();
        this.repProductName = order.getRepProductName();
    }
}
