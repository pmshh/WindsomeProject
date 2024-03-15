package com.windsome.service.product;

import com.windsome.dto.product.ProductSizeResponseDTO;
import com.windsome.repository.product.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductSizeService {

    private final ProductSizeRepository productSizeRepository;

    public List<ProductSizeResponseDTO> getProductSizesByProductId(Long productId) {
        return productSizeRepository.getProductSizesByProductId(productId);
    }
}
