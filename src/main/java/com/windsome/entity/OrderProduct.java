package com.windsome.entity;

import com.windsome.constant.OrderProductStatus;
import com.windsome.entity.auditing.BaseEntity;
import com.windsome.entity.auditing.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "order_product")
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class OrderProduct extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 상품

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 주문

    private int price; // 가격

    private int count; // 개수

    @Enumerated(EnumType.STRING)
    private OrderProductStatus orderProductStatus; // 주문 상품 현황 (주문, 취소, 교환, 반품)

    /**
     * 주문 상품 객체 생성
     */
    public static OrderProduct createOrderProduct(Product product, int count) {
        OrderProduct orderItem = OrderProduct.builder()
                .product(product)
                .price((int) Math.floor(product.getPrice() * (1-product.getDiscount())))
                .count(count)
                .orderProductStatus(OrderProductStatus.ORDER)
                .build();
        product.removeStock(count);
        return orderItem;
    }

    /**
     * 주문 취소 시 "상품 재고 복구" 및 "주문 상품 현황 변경"
     */
    public void cancel() {
        this.getProduct().addStock(count);
        this.setOrderProductStatus(OrderProductStatus.CANCELED);
    }
}
