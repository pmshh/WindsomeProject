package com.windsome.dto.order;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderPageRequestDTO {

    List<OrderPageProductRequestDTO> orderProducts = new ArrayList<>(); // 주문 상품 목록

}
