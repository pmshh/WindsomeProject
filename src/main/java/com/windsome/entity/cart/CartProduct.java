package com.windsome.entity.cart;

import com.windsome.dto.cart.CartProductDTO;
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

    private String color; // 사이즈

    private String size; // 사이즈

    private int quantity; // 상품 수량

    /**
     * Constructors, Getters, Setters, etc.
     */
    public static CartProduct createCartProduct(CartProductDTO cartProductDTO, Cart cart, Product product) {
        return CartProduct.builder()
                .cart(cart)
                .product(product)
                .color(cartProductDTO.getColor())
                .size(cartProductDTO.getSize())
                .quantity(cartProductDTO.getQuantity())
                .build();
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }
}
