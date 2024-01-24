package com.windsome.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderPageDto {
    private List<OrderPageItemDto> orders;
}
