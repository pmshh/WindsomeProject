package com.windsome.entity.product;

import com.windsome.constant.ProductSellStatus;
import com.windsome.dto.product.ProductFormDTO;
import com.windsome.entity.auditing.BaseEntity;
import com.windsome.entity.board.Board;
import com.windsome.entity.cart.CartProduct;
import com.windsome.entity.order.OrderProduct;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString(exclude = {"productImages", "reviews", "productOptions", "orderProducts", "cartProducts"})
public class Product extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 상품명

    @Column(nullable = false)
    private int price; // 상품 가격

    @Lob
    @Column(nullable = false)
    private String productDetail; // 상품 상세

    @Enumerated(EnumType.STRING)
    private ProductSellStatus productSellStatus; // 상품 판매 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cate_id")
    private Category category; // 카테고리

    private double discount; // 할인율

    private int inventory; // 재고

    @Column(precision =3, scale = 2)
    private BigDecimal averageRating; // 상품 평점

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> productOptions = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartProduct> cartProducts = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> reviews = new ArrayList<>();

    /**
     * Constructors, Getters, Setters, etc.
     */
    public void updateProduct(ProductFormDTO productFormDto, Category category, List<ProductOption> productOptionList) {
        this.name = productFormDto.getName();
        this.price = productFormDto.getPrice();
        this.inventory = productFormDto.getInventory();
        this.productDetail = productFormDto.getProductDetail();
        this.productSellStatus = productFormDto.getProductSellStatus();
        this.discount = productFormDto.getDiscount();
        this.category = category;
        this.productOptions.clear();
        this.productOptions.addAll(productOptionList);
    }

    // Product, ProductOption 연관 관계 설정
    public void setProductOptions(List<ProductOption> productOptionList) {
        this.productOptions = productOptionList;
        for (ProductOption productOption : productOptionList) {
            productOption.setProduct(this);
        }
    }
}
