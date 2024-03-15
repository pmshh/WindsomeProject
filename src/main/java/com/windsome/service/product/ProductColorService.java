package com.windsome.service.product;

import com.windsome.dto.product.ProductColorResponseDTO;
import com.windsome.repository.product.ProductColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductColorService {

    private final ProductColorRepository productColorRepository;

    public List<ProductColorResponseDTO> getProductColorsByProductId(Long productId) {
        return productColorRepository.getProductColorsByProductId(productId);
    }
}
