package com.windsome.entity.product;

import com.windsome.entity.auditing.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class ProductImage extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id")
    private Long id;

    private String serverImageName;

    private String originalImageName;

    private String imageUrl;

    private boolean isRepresentativeImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /**
     * Constructors, Getters, Setters, etc.
     */
    public void updateProductImage(String originalImageName, String serverImageName, String imageUrl) {
        this.originalImageName = originalImageName;
        this.serverImageName = serverImageName;
        this.imageUrl = imageUrl;
    }

    public ProductImage(Product product, boolean isRepresentativeImage) {
        this.product = product;
        this.isRepresentativeImage = isRepresentativeImage;
    }
}
