package com.windsome.repository.productImage;

import com.windsome.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByIdAsc(Long productId);

    ProductImage findByProductIdAndIsRepresentativeImage(Long productId, boolean param);

    Optional<List<ProductImage>> findByProductId(Long productId);
}
