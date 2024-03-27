package com.windsome.repository.product;

import com.windsome.entity.product.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    Optional<ProductOption> findByProductIdAndColorAndSize(Long id, String color, String size);

    void deleteAllByProductId(Long productId);

    List<ProductOption> findAllByProductId(Long productId);
}
