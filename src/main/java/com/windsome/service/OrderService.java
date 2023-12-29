package com.windsome.service;

import com.windsome.dto.OrderDto;
import com.windsome.entity.Account;
import com.windsome.entity.Item;
import com.windsome.entity.Order;
import com.windsome.entity.OrderItem;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ItemRepository itemRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;

    public Long order(OrderDto orderDto, String userIdentifier) {
        // 상품, 계정 DB에서 조회
        Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);
        Account account = accountRepository.findByUserIdentifier(userIdentifier);

        // 주문 상품 생성 및 저장
        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
        orderItemList.add(orderItem);

        // 주문 생성 및 DB 저장
        Order order = Order.createOrder(account, orderItemList);
        orderRepository.save(order);

        return order.getId();
    }
}
