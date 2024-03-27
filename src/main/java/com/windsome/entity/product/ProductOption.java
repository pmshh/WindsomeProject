package com.windsome.entity.product;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString(exclude = "product")
public class ProductOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String color;

    private String size;

    private int quantity;

    /**
     * Constructors, Getters, Setters, etc.
     */
    public void removeStock(int orderQuantity) {
        this.quantity -= orderQuantity;
    }
}
