package com.windsome.service.order;

import com.windsome.dto.admin.CategorySalesResult;
import com.windsome.entity.order.OrderProduct;
import com.windsome.repository.orderProduct.OrderProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderProductService {

    private final OrderProductRepository orderProductRepository;

    public List<OrderProduct> getOrderProductsByOrderId(Long orderId) {
        return orderProductRepository.findByOrderId(orderId);
    }

    public List<CategorySalesResult> getCategorySalesCount() {
        return orderProductRepository.getCategorySalesCount();
    }

    public OrderProduct getOrderProductByOrderProductId(Long orderProductId) {
        return orderProductRepository.findById(orderProductId).orElseThrow(EntityNotFoundException::new);
    }
}
