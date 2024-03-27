package com.windsome.repository.productImage;

import com.windsome.entity.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findAllByProductIdOrderByIdAsc(Long productId);

    ProductImage findByProductIdAndIsRepresentativeImage(Long productId, boolean isRepresentative);
}
