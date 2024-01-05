package com.windsome.dto;

import com.windsome.entity.Category;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryDto {

    private Long id;

    private String name;

    private String tier;

    private Long parent;

    private List<CategoryDto> children;

    /**
     * Entity -> Dto
     */
    public CategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.tier = category.getTier();
        if(category.getParent() == null) {
            this.parent = 0L;
        } else {
            this.parent = category.getParent().getId();
        }
        this.children = category.getChildren().stream().map(CategoryDto::new).collect(Collectors.toList());
    }
}
