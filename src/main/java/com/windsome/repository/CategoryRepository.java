package com.windsome.repository;

import com.windsome.dto.MainCategoryDto;
import com.windsome.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByParentIsNull();

    @Query(value = "select new com.windsome.dto.MainCategoryDto(c.id, c.name) from Category c")
    List<MainCategoryDto> mainPageCategory();
}
