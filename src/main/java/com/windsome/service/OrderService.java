package com.windsome.service;

import com.windsome.dto.order.*;
import com.windsome.entity.*;
import com.windsome.repository.cartProduct.CartProductRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.productImage.ProductImageRepository;
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
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductImageRepository productImageRepository;
    private final CartProductRepository cartProductRepository;
    private final OrderRepository orderRepository;

    /**
     * 주문 조회
     */
    @Transactional(readOnly = true)
    public Page<OrderListDto> getOrderList(String userIdentifier, Pageable pageable) {
        List<Order> orders = orderRepository.findOrders(userIdentifier, pageable);
        Long totalCount = orderRepository.countOrder(userIdentifier);

        // Order -> OrderHistDto 변환 후 반환
        List<OrderListDto> orderHistDtoList = orders.stream()
                .map(order -> {
                    OrderListDto orderHistDto = new OrderListDto(order);
                    orderHistDto.setOrderProductDtoList(order.getOrderProducts().stream()
                            .map(orderProduct -> {
                                ProductImage productImage = productImageRepository.findByProductIdAndIsRepresentativeImage(orderProduct.getProduct().getId(), true);
                                return new OrderProductDto(orderProduct, productImage.getImageUrl());
                            })
                            .collect(Collectors.toList()));
                    return orderHistDto;
                })
                .collect(Collectors.toList());
        return new PageImpl<OrderListDto>(orderHistDtoList, pageable, totalCount);
    }

    /**
     * 주문서 작성 페이지 - 주문 상품 정보
     */
    @Transactional(readOnly = true)
    public List<OrderPageProductDto> getOrderProductDetails(List<OrderPageProductDto> orders) {
        return orders.stream()
                .map(orderDto -> {
                    Product product = productRepository.findById(orderDto.getProductId()).orElseThrow(EntityNotFoundException::new);
                    ProductImage productImage = productImageRepository.findByProductIdAndIsRepresentativeImage(product.getId(), true);
                    OrderPageProductDto orderPageProductDto = OrderPageProductDto.toDto(product, productImage, orderDto);
                    orderPageProductDto.initPriceInfo();
                    return orderPageProductDto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 상품 주문
     */
    public Long order(OrderDto orderDto, String userIdentifier) {
        // 회원 정보 DB 조회
        Member member = memberRepository.findByUserIdentifier(userIdentifier);

        // 주문 상품 List 생성
        List<OrderProduct> orderProductList = createOrderProductList(orderDto.getOrders());

        // 주문 정보 초기화
        orderDto.initOrderPriceInfo();

        // 주문 관련 회원 정보 초기화 및 저장
        initializeOrderMemberInfo(member, orderDto);
        memberRepository.save(member);

        // 주문 생성 및 저장
        Order order = Order.createOrder(member, orderProductList, orderDto);
        orderRepository.save(order);

        // 장바구니 상품 삭제
        deleteCartProducts(orderProductList);

        return order.getId();
    }

    /**
     * 주문 취소 권한 검사
     */
    @Transactional(readOnly = true)
    public boolean verifyOrderCancellationPermission(Long orderId, String userIdentifier) {
        Member currentAccount = memberRepository.findByUserIdentifier(userIdentifier);
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        Member savedAccount = order.getMember();
        return StringUtils.equals(currentAccount.getUserIdentifier(), savedAccount.getUserIdentifier());
    }

    /**
     * 주문 취소
     */
    public void cancelOrder(Long orderId) {
        // 주문 취소
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        order.cancelOrder();

        // 회원 주문 관련 정보 업데이트
        // 회원 포인트, 총 주문 금액, 총 적립 포인트 복구
        Member member = order.getMember();

        // 주문 후 얻은 총 적립 포인트
        int accumulatedPoints = order.getOrderProducts().stream()
                .mapToInt(OrderProduct::getAccumulatedPoints)
                .sum();

        member.setPoint(member.getPoint() - accumulatedPoints); // 회원 포인트 복구
        member.setTotalOrderPrice(member.getTotalOrderPrice() - order.getTotalOrderPrice()); // 총 주문 금액 복구

        // 주문 시 포인트 사용한 경우 사용한 포인트 복구
        if (order.getUsePoint() >= 1) {
            member.setPoint(member.getPoint() + order.getUsePoint()); // 현재 포인트 복구
            member.setTotalUsePoint(member.getTotalUsePoint() - order.getUsePoint()); // 총 사용 포인트 복구
        }
        member.setTotalPoint(member.getTotalPoint() - accumulatedPoints); // 총 적립 포인트 복구

        memberRepository.save(member);
    }

    private List<OrderProduct> createOrderProductList(List<OrderProductDto> orderProductDtoList) {
        return orderProductDtoList.stream()
                .map(orderProductDto -> {
                    Product product = productRepository.findById(orderProductDto.getProductId()).orElseThrow(EntityNotFoundException::new);
                    return OrderProduct.createOrderProduct(product, orderProductDto.getCount());
                })
                .collect(Collectors.toList());
    }

    private void initializeOrderMemberInfo(Member member, OrderDto orderDto) {
        // 회원 포인트 적립, 총 주문 금액 저장, 사용 포인트 차감, 총 사용 포인트 저장, 총 적립 포인트 저장
        member.addPoint(orderDto); // 회원 포인트 적립
        member.setTotalOrderPrice(member.getTotalOrderPrice() + orderDto.getOrderFinalSalePrice()); // 주문 금액 저장

        if (orderDto.getUsePoint() > 0) {
            member.setPoint(member.getPoint() - orderDto.getUsePoint()); // 사용 포인트 차감
            member.setTotalUsePoint(member.getTotalUsePoint() + orderDto.getUsePoint()); // 총 사용 포인트 저장
        }

        member.setTotalPoint(member.getTotalPoint() + orderDto.getOrderSavePoint()); // 총 적립 포인트 저장
    }

    private void deleteCartProducts(List<OrderProduct> orderProductList) {
        // 장바구니 통해서 구매한 경우 장바구니에 담긴 상품 삭제
        orderProductList.forEach(orderProduct -> {
            CartProduct cartProduct = cartProductRepository.findByProductId(orderProduct.getProduct().getId());
            if (cartProduct != null) {
                cartProductRepository.delete(cartProduct);
            }
        });
    }
}
