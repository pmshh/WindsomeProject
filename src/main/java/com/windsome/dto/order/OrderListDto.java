package com.windsome.dto.order;

import com.windsome.constant.OrderStatus;
import com.windsome.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class OrderListDto {

    private Long orderId;

    private String orderDate;

    private OrderStatus orderStatus;

    private List<OrderProductDto> orderProductDtoList = new ArrayList<>();

    public OrderListDto(Order order) {
        this.orderId = order.getId();
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.orderStatus = order.getOrderStatus();
    }
}
