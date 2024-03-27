package com.windsome.service.product;

import com.windsome.entity.product.ProductOption;
import com.windsome.repository.product.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;

    /**
     * 상품 옵션 조회
     */
    public ProductOption getProductOptionByProductIdAndColorAndSize(Long productId, String color, String size) {
        return productOptionRepository.findByProductIdAndColorAndSize(productId, color, size).orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 상품 옵션 List 조회
     */
    public List<ProductOption> getProductOptionsByProductId(Long productId) {
        return productOptionRepository.findAllByProductId(productId);
    }

}
