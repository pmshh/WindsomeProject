package com.windsome.dto.admin;

import lombok.*;

import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class DashboardDataDto {

    private long totalAccount;
    private long totalItem;
    private long totalQa;
    private Long totalSales;
    private List<NumOfSalesByCateDto> numOfSalesByCateList;
}
