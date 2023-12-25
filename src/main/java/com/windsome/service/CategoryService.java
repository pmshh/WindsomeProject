package com.windsome.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsome.dto.CategoryDto;
import com.windsome.entity.Category;
import com.windsome.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public String getJsonCategories() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Category> categoryList = categoryRepository.findAllByParentIsNull();
        List<CategoryDto> categoryDtoList = categoryList.stream().map(CategoryDto::new).collect(Collectors.toList());
        return objectMapper.writeValueAsString(categoryDtoList);
    }

    public Category getCategory(Long parentCateId, Long childCateId) {
        if (childCateId != null) {
            return categoryRepository.findById(childCateId).orElseThrow(EntityNotFoundException::new);
        } else {
            return categoryRepository.findById(parentCateId).orElseThrow(EntityNotFoundException::new);
        }
    }

}
