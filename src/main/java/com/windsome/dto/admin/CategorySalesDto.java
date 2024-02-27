package com.windsome.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CategorySalesDto {

    public Long category;
    public Long count;

    public CategorySalesDto(CategorySalesResult data) {
        this.category = data.getCategory();
        this.count = data.getCount();
    }
}
