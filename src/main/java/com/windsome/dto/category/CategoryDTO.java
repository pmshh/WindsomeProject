package com.windsome.dto.category;

import com.windsome.entity.product.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

    private Long id;

    private String name;

    private String tier;

    private Long parent;

    private List<CategoryDTO> children;

    /**
     * Entity -> Dto
     */
    public CategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.tier = category.getTier();
        if(category.getParent() == null) {
            this.parent = 0L;
        } else {
            this.parent = category.getParent().getId();
        }
        this.children = category.getChildren().stream().map(CategoryDTO::new).collect(Collectors.toList());
    }
}
