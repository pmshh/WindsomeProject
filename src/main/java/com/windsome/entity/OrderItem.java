package com.windsome.entity;

import com.windsome.entity.Auditing.BaseEntity;
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

    public static OrderItem createOrderItem(Item item, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setPrice(item.getPrice());
        orderItem.setCount(count);
        orderItem.setDiscount(item.getDiscount());
        orderItem.setSavePoint((int) (item.getPrice() * (1 - item.getDiscount()) * 0.05));

        item.removeStock(count);
        return orderItem;
    }

    public int getTotalPrice() {
        return price * count;
    }

    public void cancel() {
        this.getItem().addStock(count);
    }
}
