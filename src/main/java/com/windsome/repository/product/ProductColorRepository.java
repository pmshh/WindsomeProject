package com.windsome.repository.product;

import com.windsome.dto.product.ProductColorResponseDTO;
import com.windsome.entity.product.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductColorRepository extends JpaRepository<ProductColor, Long> {

    void deleteAllByProductId(Long id);

    boolean existsByProductIdAndColorId(Long productId, Long colorId);

    @Query(value = "select new com.windsome.dto.product.ProductColorResponseDTO(pc.id, pc.product.id, pc.color.id, c.name) from ProductColor pc join Color c on pc.color.id = c.id where pc.product.id = :productId order by c.id asc")
    List<ProductColorResponseDTO> getProductColorsByProductId(@Param("productId") Long productId);

    List<ProductColor> findAllByProductId(Long productId);
}
