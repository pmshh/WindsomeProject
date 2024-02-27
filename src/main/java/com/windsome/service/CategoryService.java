package com.windsome.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsome.dto.category.CategoryDto;
import com.windsome.dto.category.MainPageCategoryDTO;
import com.windsome.entity.Category;
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
     * 메인 페이지 카테고리 조회
     */
    @Transactional(readOnly = true)
    public List<MainPageCategoryDTO> fetchCategories() {
        return categoryRepository.mainPageCategory();
    }

    /**
     * 제품 카테고리 조회
     */
    public String fetchProductCategories() throws JsonProcessingException {
        List<Category> categoryList = categoryRepository.findAllByParentIsNull();
        List<CategoryDto> categoryDtoList = categoryList.stream().map(CategoryDto::new).collect(Collectors.toList());
        return objectMapper.writeValueAsString(categoryDtoList);
    }

}
