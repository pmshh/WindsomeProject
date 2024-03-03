package com.windsome.repository.product;

import com.windsome.dto.product.MainPageProductDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> findProducts(ProductSearchDTO productSearchDto, Pageable pageable);

    Page<MainPageProductDTO> getMainPageProducts(ProductSearchDTO productSearchDto, Pageable pageable);
}
