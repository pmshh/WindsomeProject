package com.windsome.dto.admin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class DashboardDataDto {

    private long totalAccount;
    private long totalItem;
    private Long totalSales;
}
