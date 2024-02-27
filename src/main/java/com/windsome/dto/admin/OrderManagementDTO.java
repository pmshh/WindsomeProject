package com.windsome.dto.admin;

import com.windsome.constant.OrderStatus;
import com.windsome.dto.order.OrderProductDto;
import com.windsome.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderManagementDTO {

    private Long orderId;

    private String orderDate;

    private OrderStatus orderStatus;

    private String buyerId;

    private List<OrderProductDto> orderProductDtoList = new ArrayList<>();

    public OrderManagementDTO(Order order) {
        this.orderId = order.getId();
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.orderStatus = order.getOrderStatus();
        this.buyerId = order.getMember().getUserIdentifier();
    }
}
