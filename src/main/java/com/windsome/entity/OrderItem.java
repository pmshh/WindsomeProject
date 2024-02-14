package com.windsome.entity;

import com.windsome.constant.OrderItemStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.entity.auditing.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class OrderItem extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int price;

    private int count;

    private double discount;

    private int savePoint; // 적립 포인트

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderItemStatus;

    public static OrderItem createOrderItem(Item item, int count) {
        OrderItem orderItem = OrderItem.builder()
                .item(item)
                .price(item.getPrice())
                .count(count)
                .discount(item.getDiscount())
                .savePoint((int) (item.getPrice() * (1 - item.getDiscount()) * 0.05))
                .orderItemStatus(OrderItemStatus.ORDER)
                .build();
        item.removeStock(count);
        return orderItem;
    }

    public void cancel() {
        this.getItem().addStock(count);
    }
}
