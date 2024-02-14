package com.windsome.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class NumOfSalesByCateDto {

    public Long category;
    public Long count;

    public NumOfSalesByCateDto(NumOfSalesByCateDtoInterface data) {
        this.category = data.getCategory();
        this.count = data.getCount();
    }
}
