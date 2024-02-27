package com.windsome.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsome.dto.category.CategoryDto;
import com.windsome.dto.category.MainPageCategoryDTO;
import com.windsome.entity.Category;
import com.windsome.repository.category.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private CategoryService categoryService;

    @Test
    @DisplayName("메인 페이지 카테고리 조회")
    void testFetchCategories() {
        // Given
        List<MainPageCategoryDTO> expectedCategories = new ArrayList<>();
        expectedCategories.add(new MainPageCategoryDTO(1L, "카테고리1"));
        expectedCategories.add(new MainPageCategoryDTO(2L, "카테고리2"));
        expectedCategories.add(new MainPageCategoryDTO(3L, "카테고리3"));
        when(categoryRepository.mainPageCategory()).thenReturn(expectedCategories);

        // When
        List<MainPageCategoryDTO> actualCategories = categoryService.fetchCategories();

        // Then
        assertNotNull(actualCategories);
        assertEquals(expectedCategories.size(), actualCategories.size());
        for (int i = 0; i < expectedCategories.size(); i++) {
            MainPageCategoryDTO expectedCategory = expectedCategories.get(i);
            MainPageCategoryDTO actualCategory = actualCategories.get(i);
            assertEquals(expectedCategory.getId(), actualCategory.getId());
            assertEquals(expectedCategory.getName(), actualCategory.getName());
        }
        verify(categoryRepository, times(1)).mainPageCategory();
    }

    @Test
    @DisplayName("제품 카테고리 조회")
    void testFetchProductCategories() throws JsonProcessingException {
        // Given
        Category category1 = new Category(1L, "카테고리1", null, null, new ArrayList<>());
        Category category2 = new Category(2L, "카테고리2", null, null, new ArrayList<>());
        List<Category> categories = Arrays.asList(category1, category2);

        List<CategoryDto> expectedCategoryDtos = categories.stream()
                .map(CategoryDto::new)
                .collect(Collectors.toList());

        given(categoryRepository.findAllByParentIsNull()).willReturn(categories);
        given(objectMapper.writeValueAsString(expectedCategoryDtos)).willReturn("dummyJsonString");

        // When
        String result = categoryService.fetchProductCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("dummyJsonString"); // 예상한 값과 일치하는지 확인
        verify(categoryRepository).findAllByParentIsNull();
    }
}