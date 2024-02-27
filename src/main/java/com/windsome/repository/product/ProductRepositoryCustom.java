package com.windsome.repository.product;

import com.windsome.dto.product.MainPageProductDTO;
import com.windsome.dto.product.ProductSearchDto;
import com.windsome.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> findProducts(ProductSearchDto productSearchDto, Pageable pageable);

    Page<MainPageProductDTO> getMainPageProducts(ProductSearchDto productSearchDto, Pageable pageable);
}
