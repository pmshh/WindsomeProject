package com.windsome.entity;

import com.windsome.constant.ProductSellStatus;
import com.windsome.dto.product.ProductFormDto;
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
@ToString(exclude = {"productImages", "reviews"})
public class Product extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stockNumber;

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

    public void updateProduct(ProductFormDto productFormDto) {
        this.name = productFormDto.getProductName();
        this.price = productFormDto.getPrice();
        this.stockNumber = productFormDto.getStockNumber();
        this.productDetail = productFormDto.getProductDetail();
        this.productSellStatus = productFormDto.getProductSellStatus();
        this.discount = productFormDto.getDiscount();
    }

    public void removeStock(int stockNumber) {
        int restStock = this.stockNumber - stockNumber;
        if (restStock <= 0) {
            this.productSellStatus = ProductSellStatus.SOLD_OUT;
        }
        this.stockNumber = restStock;
    }

    public void addStock(int stockNumber) {
        this.stockNumber += stockNumber;
    }
}
