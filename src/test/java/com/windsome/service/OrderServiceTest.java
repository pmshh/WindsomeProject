package com.windsome.service;

import com.windsome.constant.ItemSellStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.dto.order.OrderDto;
import com.windsome.dto.order.OrderHistDto;
import com.windsome.dto.order.OrderItemDto;
import com.windsome.entity.*;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.ItemImgRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class OrderServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ItemImgRepository itemImgRepository;

    @Test
    @DisplayName("주문 테스트")
    public void order() {
        // given
        Item item = saveItem();
        Account account = saveAccount();
        OrderDto orderDto = getOrderDto(item);

        // when
        Long orderId = orderService.order(orderDto, account.getUserIdentifier());
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        // then
        assertEquals(order.getAddress1(), orderDto.getAddress1());
        assertEquals(order.getTotalOrderPrice(), orderDto.getOrderFinalSalePrice());
        assertEquals(90, item.getStockNumber());
    }

    @Test
    @DisplayName("주문 취소 테스트")
    public void cancelOrder() {
        // given
        Account account = saveAccount();
        Item item = saveItem();
        OrderDto orderDto = getOrderDto(item);

        // when
        Long orderId = orderService.order(orderDto, account.getUserIdentifier());
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        orderService.cancelOrder(orderId);

        // then
        assertEquals(OrderStatus.CANCEL, order.getOrderStatus());
        assertEquals(100, item.getStockNumber());
    }

    @Test
    @DisplayName("주문 조회 테스트")
    public void getOrderTest() {
        // given
        Account account = saveAccount();
        Item item1 = saveItem();
        Item item2 = saveItem();
        saveItemImg(item1);
        saveItemImg(item2);
        OrderDto orderDto1 = getOrderDto(item1);
        OrderDto orderDto2 = getOrderDto(item2);

        // when
        orderService.order(orderDto1, account.getUserIdentifier());
        orderService.order(orderDto2, account.getUserIdentifier());

        Pageable pageable = PageRequest.of(0, 4);
        Page<OrderHistDto> orderList = orderService.getOrderList(account.getUserIdentifier(), pageable);

        // then
        assertEquals(orderList.getTotalElements(), 2);
        assertEquals(orderList.getSize(), 4);
    }

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

    private void saveItemImg(Item item) {
        ItemImg itemImg = ItemImg.builder()
                .item(item)
                .oriImgName("test")
                .imgName("test")
                .imgUrl("test")
                .repImgYn("Y")
                .build();
        itemImgRepository.save(itemImg);
    }

    private static OrderDto getOrderDto(Item item) {
        List<OrderItemDto> orderItemDtoList = new ArrayList<>();
        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setItemId(item.getId());
        orderItemDto.setPrice(item.getPrice());
        orderItemDto.setDiscount(item.getDiscount());
        orderItemDto.setCount(10);
        orderItemDtoList.add(orderItemDto);
        orderItemDto.initPriceAndPoint();

        OrderDto orderDto = new OrderDto("test", "test", "test", "test", "test", "test", orderItemDtoList, 0, 0, 10000, 500, 10000);
        orderDto.initOrderPriceInfo();
        return orderDto;
    }
}