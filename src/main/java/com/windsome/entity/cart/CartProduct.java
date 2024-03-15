package com.windsome.entity.cart;

import com.windsome.entity.Color;
import com.windsome.entity.Size;
import com.windsome.entity.auditing.BaseTimeEntity;
import com.windsome.entity.product.Product;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class CartProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart; // 장바구니

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 상품

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private Color color; // 색상

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id")
    private Size size; // 사이즈

    private int quantity; // 상품 수량

    public static CartProduct createCartProduct(Cart cart, Product product, Color color, Size size, int quantity) {
        return CartProduct.builder()
                .cart(cart)
                .product(product)
                .color(color)
                .size(size)
                .quantity(quantity)
                .build();
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }
}
