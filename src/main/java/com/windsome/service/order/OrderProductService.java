package com.windsome.service.order;

import com.windsome.entity.order.OrderProduct;
import com.windsome.repository.orderProduct.OrderProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderProductService {

    private final OrderProductRepository orderProductRepository;

    /**
     * OrderService - 주문 상품 목록 조회
     */
    public List<OrderProduct> getOrderProductsByOrderId(Long orderId) {
        return orderProductRepository.findByOrderId(orderId);
    }
}
