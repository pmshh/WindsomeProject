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
@ToString(exclude = "orderProducts")
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

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
    private List<OrderProduct> orderProducts = new ArrayList<>();

    public static Order createOrder(Member member, List<OrderProduct> orderProductList, OrderDto orderDto) {
        Order order = Order.builder()
                .member(member)
                .totalOrderPrice(orderDto.getOrderFinalSalePrice())
                .deliveryCost(orderDto.getDeliveryCost())
                .usePoint(orderDto.getUsePoint())
                .address1(orderDto.getAddress1())
                .address2(orderDto.getAddress2())
                .address3(orderDto.getAddress3())
                .tel(orderDto.getTel())
                .email(orderDto.getEmail())
                .req(orderDto.getReq())
                .orderStatus(OrderStatus.READY)
                .orderDate(LocalDateTime.now())
                .build();
        order.addOrderProduct(orderProductList);
        return order;
    }

    public void addOrderProduct(List<OrderProduct> orderProducts) {
        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.setOrder(this);
        }
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL;

        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.cancel();
        }
    }
}
