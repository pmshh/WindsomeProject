package com.windsome.service;

import com.windsome.constant.ItemSellStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.dto.OrderDto;
import com.windsome.entity.Account;
import com.windsome.entity.Item;
import com.windsome.entity.Order;
import com.windsome.entity.OrderItem;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private AccountRepository accountRepository;

    public Item saveItem() {
        Item item = Item.builder()
                .itemNm("테스트 상품")
                .price(10000)
                .itemDetail("테스트 상품 상세 설명")
                .itemSellStatus(ItemSellStatus.SELL)
                .stockNumber(100)
                .build();
        return itemRepository.save(item);
    }

    public Account saveAccount() {
        Account account = Account.builder()
                .userIdentifier("gildong123")
                .password("gildong123")
                .name("gildong")
                .email("gildong@naver.com")
                .address1("test")
                .address2("test")
                .address3("test")
                .build();
        return accountRepository.save(account);
    }

    @Test
    @DisplayName("주문 테스트")
    public void order() {
        Item item = saveItem();
        Account account = saveAccount();

        OrderDto orderDto = new OrderDto();
        orderDto.setCount(10);
        orderDto.setItemId(item.getId());

        Long orderId = orderService.order(orderDto, account.getUserIdentifier());

        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        List<OrderItem> orderItems = order.getOrderItems();

        int totalPrice = orderDto.getCount() * item.getPrice();

        assertEquals(totalPrice, order.getTotalPrice());
    }

    @Test
    @DisplayName("주문 취소 테스트")
    public void cancelOrder() {
        Item item = saveItem();
        Account account = saveAccount();

        OrderDto orderDto = new OrderDto();
        orderDto.setCount(10);
        orderDto.setItemId(item.getId());

        Long orderId = orderService.order(orderDto, account.getUserIdentifier());

        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        orderService.cancelOrder(orderId);
        assertEquals(OrderStatus.CANCEL, order.getOrderStatus());
        assertEquals(100, item.getStockNumber());
    }
}