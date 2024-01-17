package com.windsome.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPageInfoDto {

    private Long totalOrderPrice;
    private int totalPoint;
}
