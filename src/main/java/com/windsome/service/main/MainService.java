package com.windsome.service.main;

import com.windsome.dto.category.MainPageCategoryDTO;
import com.windsome.dto.product.MainPageProductDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.service.product.CategoryService;
import com.windsome.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MainService {

    private final ProductService productService;
    private final CategoryService categoryService;

    /**
     * 상품 전체 조회
     */
    public Page<MainPageProductDTO> getMainPageProducts(ProductSearchDTO productSearchDto, Pageable pageable) {
        return productService.getMainPageProducts(productSearchDto, pageable);
    }

    /**
     * 카테고리 전체 조회
     */
    public List<MainPageCategoryDTO> getCategories() {
        return categoryService.getCategories();
    }
}
