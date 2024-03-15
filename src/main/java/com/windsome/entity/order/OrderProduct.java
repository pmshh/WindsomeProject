package com.windsome.entity.order;

import com.windsome.constant.OrderProductStatus;
import com.windsome.entity.Color;
import com.windsome.entity.Size;
import com.windsome.entity.auditing.BaseTimeEntity;
import com.windsome.entity.product.Product;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "order_product")
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString(exclude = "order")
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

    private int orderQuantity; // 주문 개수

    @Enumerated(EnumType.STRING)
    private OrderProductStatus orderProductStatus; // 주문 상품 현황 (주문, 취소, 교환, 반품)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id")
    private Size size;

    /**
     * 주문 상품 객체 생성
     */
    public static OrderProduct createOrderProduct(Product product, int orderQuantity, Color color, Size size) {
        return OrderProduct.builder()
                .product(product)
                .price((int) Math.floor(product.getPrice() * (1-product.getDiscount())))
                .orderQuantity(orderQuantity)
                .color(color)
                .size(size)
                .orderProductStatus(OrderProductStatus.ORDER)
                .build();
    }
}
