package com.windsome.service;

import com.windsome.constant.OrderProductStatus;
import com.windsome.dto.order.*;
import com.windsome.dto.product.ProductInfoResponseDTO;
import com.windsome.entity.*;
import com.windsome.repository.payment.PaymentRepository;
import com.windsome.repository.cartProduct.CartProductRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.repository.orderProduct.OrderProductRepository;
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
import java.util.List;
import java.util.Objects;
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
    private final PaymentRepository paymentRepository;
    private final OrderProductRepository orderProductRepository;

    /**
     * 주문 조회
     */
    @Transactional(readOnly = true)
    public Page<OrderHistResponseDTO> getOrderList(String userIdentifier, Pageable pageable) {
        List<Order> orders = orderRepository.findOrders(userIdentifier, pageable);
        Long totalCount = orderRepository.countOrder(userIdentifier);

        // Entity -> DTO 변환
        List<OrderHistResponseDTO> orderHistDtoList = orders.stream()
                .map(order -> {
                    // List<Order> orders -> List<OrderHistDto> orderHistDtoList
                    OrderHistResponseDTO orderHistDto = new OrderHistResponseDTO(order);
                    // orderProductDTOList 값 채우기
                    orderHistDto.setOrderHistProductList(order.getOrderProducts().stream()
                            .map(orderProduct -> {
                                ProductInfoResponseDTO productInfo = productRepository.getProductInfoByProductId(orderProduct.getProduct().getId());
                                return new OrderHistProductResponseDTO(orderProduct, productInfo);
                            })
                            .collect(Collectors.toList()));
                    return orderHistDto;
                })
                .collect(Collectors.toList());
        return new PageImpl<OrderHistResponseDTO>(orderHistDtoList, pageable, totalCount);
    }

    /**
     * 주문서 작성 페이지 - 주문 상품 정보
     */
    @Transactional(readOnly = true)
    public List<OrderPageProductResponseDTO> getOrderProductDetails(List<OrderPageProductRequestDTO> orderProducts) {
        return orderProducts.stream()
                .map(orderProduct -> {
                    Product product = productRepository.findById(orderProduct.getProductId()).orElseThrow(EntityNotFoundException::new);
                    ProductImage productImage = productImageRepository.findByProductIdAndIsRepresentativeImage(product.getId(), true);
                    return OrderPageProductResponseDTO.toDto(product, productImage.getImageUrl(), orderProduct.getCount());
                })
                .collect(Collectors.toList());
    }

    /**
     * 상품 주문
     */
    public Long order(OrderRequestDTO orderRequestDTO, String userIdentifier) {
        // 회원 정보 DB 조회
        Member member = memberRepository.findByUserIdentifier(userIdentifier);

        // DB에 저장할 주문 상품 리스트 생성
        List<OrderProduct> orderProductList = createOrderProductList(orderRequestDTO.getOrderProductDtoList());

        // 회원 포인트 관련 정보 업데이트
        initializePointData(member, orderRequestDTO);
        memberRepository.save(member);

        // 결제 정보 저장
        Payment payment = Payment.createPayment(orderRequestDTO);
        paymentRepository.save(payment);

        // 주문 생성 및 저장
        Order order = Order.createOrder(member, orderProductList, orderRequestDTO, payment);
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
        // 현재 로그인 한 계정과, 주문한 계정 비교
        Member currentAccount = memberRepository.findByUserIdentifier(userIdentifier);
        Member orderAccount = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new).getMember();
        return !StringUtils.equals(currentAccount.getUserIdentifier(), orderAccount.getUserIdentifier());
    }

    /**
     * 주문 취소
     */
    public void cancelOrder(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        // 회원 포인트 관련 정보 복구
        Member member = order.getMember();
        member.setAvailablePoints(member.getAvailablePoints() - order.getEarnedPoints()); // 얻은 포인트 회수
        if (order.getUsedPoints() >= 1) {
            member.setAvailablePoints(member.getAvailablePoints() + order.getUsedPoints()); // 사용 가능한 포인트 복구
            member.setTotalUsedPoints(member.getTotalUsedPoints() - order.getUsedPoints()); // 총 사용 포인트 복구
        }
        member.setTotalEarnedPoints(member.getTotalEarnedPoints() - order.getEarnedPoints()); // 총 적립 포인트 복구
        memberRepository.save(member);

        // 주문 취소
        order.cancelOrder();
    }

    private List<OrderProduct> createOrderProductList(List<OrderProductRequestDTO> orderProductRequestDTOList) {
        return orderProductRequestDTOList.stream()
                .map(orderProductRequestDto -> {
                    Product product = productRepository.findById(orderProductRequestDto.getId()).orElseThrow(EntityNotFoundException::new);
                    return OrderProduct.createOrderProduct(product, orderProductRequestDto.getCount());
                })
                .collect(Collectors.toList());
    }

    private void initializePointData(Member member, OrderRequestDTO orderRequestDTO) {
        member.addPoint(orderRequestDTO); // 포인트 적립
        if (orderRequestDTO.getUsedPoints() > 0) {
            member.setAvailablePoints(member.getAvailablePoints() - orderRequestDTO.getUsedPoints()); // 사용 가능한 포인트 차감
            member.setTotalUsedPoints(member.getTotalUsedPoints() + orderRequestDTO.getUsedPoints()); // 총 사용 포인트 증가
        }
        member.setTotalEarnedPoints(member.getTotalEarnedPoints() + orderRequestDTO.getEarnedPoints()); // 총 적립 포인트 증가
    }

    private void deleteCartProducts(List<OrderProduct> orderProductList) {
        orderProductList.stream()
                .map(orderProduct -> cartProductRepository.findByProductId(orderProduct.getProduct().getId()))
                .filter(Objects::nonNull)
                .forEach(cartProductRepository::delete);
    }

    /**
     * 주문 상세 조회
     */
    public OrderDetailDTO getOrderDetail(Long orderId) {
        OrderDetailDTO orderDetail = orderRepository.getOrderDetail(orderId);
        List<OrderProduct> orderProductList = orderProductRepository.findByOrderId(orderId);

        List<OrderDetailProductDTO> orderDetailProductDTOList = orderProductList.stream().map(orderProduct -> {
            OrderDetailProductDTO orderDetailProductDTO = new OrderDetailProductDTO();
            ProductImage productImage = productImageRepository.findByProductIdAndIsRepresentativeImage(orderProduct.getProduct().getId(), true);
            orderDetailProductDTO.setImageUrl(productImage.getImageUrl());
            orderDetailProductDTO.setProductId(orderProduct.getProduct().getId());
            orderDetailProductDTO.setProductName(orderProduct.getProduct().getName());
            orderDetailProductDTO.setOrderQuantity(orderProduct.getCount());
            orderDetailProductDTO.setProductPrice(orderProduct.getPrice());
            orderDetailProductDTO.setOrderProductStatus(OrderProductStatus.ORDER);
            if (orderProduct.getPrice() * orderProduct.getCount() > 30000) {
                orderDetailProductDTO.setDeliveryPrice(0);
            }
            return orderDetailProductDTO;
        }).collect(Collectors.toList());

        orderDetail.setOrderDetailProductList(orderDetailProductDTOList);
        return orderDetail;
    }
}
