package com.windsome.entity.product;

import com.windsome.constant.ProductSellStatus;
import com.windsome.dto.product.ProductFormDTO;
import com.windsome.entity.Category;
import com.windsome.entity.auditing.BaseEntity;
import com.windsome.entity.board.Review;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString(exclude = {"productImages", "reviews", "colors", "sizes", "inventories"})
public class Product extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int price;

    @Lob
    @Column(nullable = false)
    private String productDetail;

    @Enumerated(EnumType.STRING)
    private ProductSellStatus productSellStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cate_id")
    private Category category;

    private double discount;

    @Column(precision =3, scale = 2)
    private BigDecimal averageRating;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductColor> colors = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSize> sizes = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inventory> inventories = new ArrayList<>();

    /**
     * Constructors, Getters, Setters, etc.
     */
    public void updateProduct(ProductFormDTO productFormDto) {
        this.name = productFormDto.getName();
        this.price = productFormDto.getPrice();
        this.productDetail = productFormDto.getProductDetail();
        this.productSellStatus = productFormDto.getProductSellStatus();
        this.discount = productFormDto.getDiscount();
    }
}
