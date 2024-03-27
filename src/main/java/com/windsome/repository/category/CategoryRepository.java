package com.windsome.repository.category;

import com.windsome.dto.category.MainPageCategoryDTO;
import com.windsome.entity.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByParentIsNull();

    @Query(value = "select new com.windsome.dto.category.MainPageCategoryDTO(c.id, c.name) from Category c")
    List<MainPageCategoryDTO> mainPageCategory();
}
