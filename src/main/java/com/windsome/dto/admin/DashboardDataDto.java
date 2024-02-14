package com.windsome.dto.admin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Builder
public class DashboardDataDto {

    private long totalAccount;
    private long totalItem;
    private long totalQa;
    private Long totalSales;
    private List<NumOfSalesByCateDto> numOfSalesByCateList;
}
