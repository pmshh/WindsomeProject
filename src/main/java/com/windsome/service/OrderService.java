package com.windsome.service;

import com.windsome.dto.order.*;
import com.windsome.entity.*;
import com.windsome.repository.*;
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
    private final CartItemRepository cartItemRepository;

    /**
     * 주문서 작성 페이지 - 주문 상품 정보
     */
    public List<OrderPageItemDto> getOrderItemsInfo(List<OrderPageItemDto> orders) {
        List<OrderPageItemDto> orderPageItemDtoList = new ArrayList<>();
        for (OrderPageItemDto orderDto : orders) {
            Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);
            ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(item.getId(), "Y");

            OrderPageItemDto orderPageItemDto = OrderPageItemDto.createOrderPageItemDto(item, itemImg, orderDto);
            orderPageItemDto.initPriceInfo();

            orderPageItemDtoList.add(orderPageItemDto);
        }
        return orderPageItemDtoList;
    }

    /**
     * 상품 주문
     */
    public Long order(OrderDto orderDto, String userIdentifier) {
        // 회원 정보 DB 조회
        Account account = accountRepository.findByUserIdentifier(userIdentifier);

        // 주문 상품 List 생성
        List<OrderItem> orderItemList = new ArrayList<>();
        for (OrderItemDto orderItemDto : orderDto.getOrders()) {
            Item item = itemRepository.findById(orderItemDto.getItemId()).orElseThrow(EntityNotFoundException::new);

            OrderItem orderItem = OrderItem.createOrderItem(item, orderItemDto.getCount());
            orderItemList.add(orderItem);

            orderItemDto.setPrice(item.getPrice());
            orderItemDto.setDiscount(item.getDiscount());
            orderItemDto.initPriceAndPoint();
        }
        orderDto.initOrderPriceInfo();

        // 회원 포인트 적립, 총 주문 금액 저장, 사용 포인트 차감, 총 사용 포인트 저장, 총 적립 포인트 저장
        Account saveAccount = Account.addPoint(account, orderDto); // 회원 포인트 적립
        saveAccount.setTotalOrderPrice(saveAccount.getTotalOrderPrice() + orderDto.getOrderFinalSalePrice()); // 주문 금액 저장
        if (orderDto.getUsePoint() > 0) {
            saveAccount.setPoint(saveAccount.getPoint() - orderDto.getUsePoint()); // 사용 포인트 차감
            saveAccount.setTotalUsePoint(saveAccount.getTotalUsePoint() + orderDto.getUsePoint()); // 총 사용 포인트 저장
        }
        saveAccount.setTotalPoint(saveAccount.getTotalPoint() + orderDto.getOrderSavePoint()); // 총 적립 포인트 저장
        accountRepository.save(saveAccount);

        // 상품 생성 및 DB 저장
        Order order = Order.createOrder(account, orderItemList, orderDto);
        orderRepository.save(order);

        // 장바구니 상품 삭제
        for (OrderItem orderItem : orderItemList) {
            CartItem cartItem = cartItemRepository.findByItemId(orderItem.getItem().getId());
            if (cartItem != null) {
                cartItemRepository.delete(cartItem);
            }
        }
        return order.getId();
    }

    /**
     * 주문 취소 위한 권한 검사
     */
    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String userIdentifier) {
        Account currentAccount = accountRepository.findByUserIdentifier(userIdentifier);
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        Account savedAccount = order.getAccount();

        return StringUtils.equals(currentAccount.getUserIdentifier(), savedAccount.getUserIdentifier());
    }

    /**
     * 주문 취소
     */
    public void cancelOrder(Long orderId) {
        // 주문 취소
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        order.cancelOrder();

        // 회원 포인트 복구, 총 주문 금액 복구, 현재 포인트 복구, 총 적립 포인트 복구, 총 사용 포인트 복구
        Account findAccount = accountRepository.findById(order.getAccount().getId()).orElseThrow(EntityNotFoundException::new);
        int savePoint = 0;
        for (OrderItem orderItem : order.getOrderItems()) {
            savePoint += orderItem.getSavePoint();
        }
        findAccount.setPoint(findAccount.getPoint() - savePoint); // 회원 포인트 복구
        findAccount.setTotalOrderPrice(findAccount.getTotalOrderPrice() - order.getTotalOrderPrice()); // 총 주문 금액 복구

        if (order.getUsePoint() >= 1) {
            findAccount.setPoint(findAccount.getPoint() + order.getUsePoint()); // 현재 포인트 복구
            findAccount.setTotalUsePoint(findAccount.getTotalUsePoint() - order.getUsePoint()); // 총 사용 포인트 복구
        }
        findAccount.setTotalPoint(findAccount.getTotalPoint() - savePoint); // 총 적립 포인트 복구
    }

    /**
     * 주문 조회 - 회원
     */
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

    /**
     * 주문 조회 - 관리자 페이지
     */
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
}
