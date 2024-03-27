package com.windsome.service.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsome.dto.category.CategoryDTO;
import com.windsome.dto.category.MainPageCategoryDTO;
import com.windsome.entity.product.Category;
import com.windsome.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final ObjectMapper objectMapper;

    /**
     * 카테고리 전체 조회
     */
    @Transactional(readOnly = true)
    public List<MainPageCategoryDTO> getCategories() {
        return categoryRepository.mainPageCategory();
    }

    /**
     * 카테고리 단건 조회
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow();
    }

    /**
     * 관리자 페이지 - 상품 등록/수정 카테고리 조회
     */
    public String getProductCategories() throws JsonProcessingException {
        List<Category> categoryList = categoryRepository.findAllByParentIsNull();
        List<CategoryDTO> categoryDTOList = categoryList.stream().map(CategoryDTO::new).collect(Collectors.toList());
        return objectMapper.writeValueAsString(categoryDTOList);
    }
}
