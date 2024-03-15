package com.windsome.repository.product;

import com.windsome.dto.product.ProductSizeResponseDTO;
import com.windsome.entity.product.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {
    void deleteAllByProductId(Long id);

    @Query(value = "select new com.windsome.dto.product.ProductSizeResponseDTO(ps.id, ps.product.id, ps.size.id, s.name) from ProductSize ps join Size s on ps.size.id = s.id where ps.product.id = :productId order by s.id asc")
    List<ProductSizeResponseDTO> getProductSizesByProductId(@Param("productId") Long productId);

    boolean existsByProductIdAndSizeId(Long productId, Long sizeId);
}
