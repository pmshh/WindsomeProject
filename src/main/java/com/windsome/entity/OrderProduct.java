package com.windsome.entity;

import com.windsome.constant.OrderProductStatus;
import com.windsome.entity.auditing.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "order_product")
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class OrderProduct extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int price;

    private int count;

    private double discount;

    private int accumulatedPoints;

    @Enumerated(EnumType.STRING)
    private OrderProductStatus orderProductStatus;

    public static OrderProduct createOrderProduct(Product product, int count) {
        OrderProduct orderItem = OrderProduct.builder()
                .product(product)
                .price(product.getPrice())
                .count(count)
                .discount(product.getDiscount())
                .accumulatedPoints((int) (product.getPrice() * (1 - product.getDiscount()) * 0.05))
                .orderProductStatus(OrderProductStatus.ORDER)
                .build();
        product.removeStock(count);
        return orderItem;
    }

    public void cancel() {
        this.getProduct().addStock(count);
    }
}
