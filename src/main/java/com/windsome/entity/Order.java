package com.windsome.entity;

import com.windsome.constant.OrderStatus;
import com.windsome.dto.order.OrderDto;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private LocalDateTime orderDate;

    private int totalOrderPrice;

    private int deliveryCost;

    private int usePoint;

    private String address1;

    private String address2;

    private String address3;

    private String tel;

    private String email;

    private String req;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrder(this);
        }
    }

    public static Order createOrder(Account account, List<OrderItem> orderItemList, OrderDto orderDto) {
        Order order = Order.builder()
                .account(account)
                .totalOrderPrice(orderDto.getOrderFinalSalePrice())
                .deliveryCost(orderDto.getDeliveryCost())
                .usePoint(orderDto.getUsePoint())
                .address1(orderDto.getAddress1())
                .address2(orderDto.getAddress2())
                .address3(orderDto.getAddress3())
                .tel(orderDto.getTel())
                .email(orderDto.getEmail())
                .req(orderDto.getReq())
                .orderItems(orderItemList)
                .orderStatus(OrderStatus.READY)
                .orderDate(LocalDateTime.now())
                .build();
        order.addOrderItem(orderItemList);
        return order;
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL;

        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }
}
