package com.windsome.entity;

import com.windsome.constant.OrderStatus;
import com.windsome.dto.OrderDto;
import com.windsome.entity.Auditing.BaseEntity;
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

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public static Order createOrder(Account account, List<OrderItem> orderItemList, OrderDto orderDto) {
        Order order = new Order();
        order.setAccount(account);
        order.setTotalOrderPrice(orderDto.getOrderFinalSalePrice());
        order.setDeliveryCost(orderDto.getDeliveryCost());
        order.setUsePoint(orderDto.getUsePoint());
        order.setAddress1(orderDto.getAddress1());
        order.setAddress2(orderDto.getAddress2());
        order.setAddress3(orderDto.getAddress3());
        order.setTel(orderDto.getTel());
        order.setEmail(orderDto.getEmail());
        order.setReq(orderDto.getReq());
        for (OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);
        }
        order.setOrderStatus(OrderStatus.READY);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL;

        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }
}
