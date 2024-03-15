package com.windsome.dto.order;

import com.windsome.constant.OrderStatus;
import com.windsome.entity.order.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class OrderHistResponseDTO {

    private Long orderId; // 주문 id

    private String orderUid; // 주문 번호

    private String orderDate; // 주문 날짜

    private OrderStatus orderStatus; // 주문 현황

    private List<OrderHistProductResponseDTO> orderHistProductList = new ArrayList<>(); // 주문 품목

    public OrderHistResponseDTO(Order order) {
        this.orderId = order.getId();
        this.orderUid = order.getOrderUid();
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.orderStatus = order.getOrderStatus();
    }
}
