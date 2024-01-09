package com.windsome.service;

import com.windsome.dto.*;
import com.windsome.entity.*;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.ItemImgRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

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
    private final ItemImgRepository itemImgRepository;

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

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        order.cancelOrder();
    }

    public Page<OrderHistDto> getOrderList(String userIdentifier, Pageable pageable) {
        List<Order> orders = orderRepository.findOrders(userIdentifier, pageable);
        Long totalCount = orderRepository.countOrder(userIdentifier);

        // List<Order> orders -> List<OrderHistDto> orderHistDtoList 변환
        List<OrderHistDto> orderHistDtoList = new ArrayList<>();

        for (Order order : orders) {
            // Entity -> Dto 변환
            OrderHistDto orderHistDto = new OrderHistDto(order);

            // orderHistDto - orderItemDtoList 필드에 값 채우기
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());

                orderHistDto.addOrderItemDto(orderItemDto);
            }
            // orderHistDtoList - orderHistDto 추가
            orderHistDtoList.add(orderHistDto);
        }
        return new PageImpl<OrderHistDto>(orderHistDtoList, pageable, totalCount);
    }

    public Page<OrderMngDto> getAdminPageOrderList(String userIdentifier, Pageable pageable) {
        List<Order> orders = orderRepository.findOrderListForAdmin(userIdentifier, pageable);
        Long totalCount = orderRepository.countOrderList(userIdentifier);

        // List<Order> orders -> List<OrderMngDto> orderMngDtoList 변환
        List<OrderMngDto> orderMngDtoList = new ArrayList<>();

        for (Order order : orders) {
            // Entity -> Dto 변환
            OrderMngDto orderMngDto = new OrderMngDto(order);

            // orderMngDto - orderItemDtoList 필드에 값 채우기
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());

                orderMngDto.addOrderItemDto(orderItemDto);
            }

            // orderMngDtoList - orderMngDto 추가
            orderMngDtoList.add(orderMngDto);
        }
        return new PageImpl<OrderMngDto>(orderMngDtoList, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String userIdentifier) {
        Account currentAccount = accountRepository.findByUserIdentifier(userIdentifier);
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        Account savedAccount = order.getAccount();

        return StringUtils.equals(currentAccount.getUserIdentifier(), savedAccount.getUserIdentifier());
    }

    public Long orders(List<OrderDto> orderDtoList, String userIdentifier) {
        Account account = accountRepository.findByUserIdentifier(userIdentifier);

        List<OrderItem> orderItemList = new ArrayList<>();
        for (OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);

            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
            orderItemList.add(orderItem);
        }

        Order order = Order.createOrder(account, orderItemList);
        orderRepository.save(order);

        return order.getId();
    }
}
