package com.windsome.dto.order;

import lombok.Data;

import java.util.List;

@Data
public class OrderPageDto {
    private List<OrderPageItemDto> orders;
}
