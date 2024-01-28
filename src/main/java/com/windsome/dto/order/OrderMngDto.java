package com.windsome.dto.order;

import com.windsome.constant.OrderStatus;
import com.windsome.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderMngDto {

    private Long orderId;

    private String orderDate;

    private OrderStatus orderStatus;

    private String buyerId;

    private List<OrderItemDto> orderItemDtoList = new ArrayList<>();

    public OrderMngDto(Order order) {
        this.orderId = order.getId();
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.orderStatus = order.getOrderStatus();
        this.buyerId = order.getAccount().getUserIdentifier();
    }

    public void addOrderItemDto(OrderItemDto orderItemDto) {
        orderItemDtoList.add(orderItemDto);
    }
}
