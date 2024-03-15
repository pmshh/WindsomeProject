package com.windsome.service;

import com.windsome.constant.OrderProductStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.constant.PaymentStatus;
import com.windsome.constant.ProductSellStatus;
import com.windsome.dto.order.*;
import com.windsome.dto.product.ProductInfoResponseDTO;
import com.windsome.entity.Color;
import com.windsome.entity.product.Inventory;
import com.windsome.entity.Size;
import com.windsome.entity.cart.CartProduct;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.entity.order.OrderProduct;
import com.windsome.entity.order.Payment;
import com.windsome.entity.product.Product;
import com.windsome.entity.product.ProductImage;
import com.windsome.repository.payment.PaymentRepository;
import com.windsome.repository.cartProduct.CartProductRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.repository.orderProduct.OrderProductRepository;
import com.windsome.repository.product.ColorRepository;
import com.windsome.repository.product.InventoryRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.product.SizeRepository;
import com.windsome.repository.productImage.ProductImageRepository;
import com.windsome.service.product.ProductColorService;
import com.windsome.service.product.ProductSizeService;
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
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductColorService productColorService;
    private final ProductSizeService productSizeService;

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
     * 주문서 작성 화면 - 주문 상품 정보 조회
     */
    public List<OrderProductResponseDTO> getOrderProductsInfo(OrderProductListDTO orderProductListDTO) {
        return orderProductListDTO.getOrderProducts().stream()
                .map(orderProductDTO -> {
                    Product product = productRepository.findById(orderProductDTO.getProductId()).orElseThrow(EntityNotFoundException::new);
                    ProductImage productImage = productImageRepository.findByProductIdAndIsRepresentativeImage(orderProductDTO.getProductId(), true);
                    CartProduct cartProduct = cartProductRepository.findByProductIdAndColorIdAndSizeId(product.getId(), orderProductDTO.getColorId(), orderProductDTO.getSizeId());
                    return OrderProductResponseDTO.builder()
                            .id(product.getId())
                            .price(product.getPrice())
                            .discount(product.getDiscount())
                            .name(product.getName())
                            .imageUrl(productImage.getImageUrl())
                            .colorId(orderProductDTO.getColorId())
                            .colorName(orderProductDTO.getColorName())
                            .sizeId(orderProductDTO.getSizeId())
                            .sizeName(orderProductDTO.getSizeName())
                            .orderQuantity(orderProductDTO.getOrderQuantity())
                            .cartProductId(cartProduct != null ? cartProduct.getId() : null)
                            .build();
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

        // 주문 취소 (주문 상태 변경, 결제 상태 변경, 주문 상품 상태 변경, 재고 수량 복구)
        order.setOrderStatus(OrderStatus.CANCELED);
        order.getPayment().setStatus(PaymentStatus.PAYMENT_CANCELLED);
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            Inventory inventory = inventoryRepository.findByProductIdAndColorIdAndSizeId(orderProduct.getProduct().getId(), orderProduct.getColor().getId(), orderProduct.getSize().getId());
            inventory.setQuantity(inventory.getQuantity() + orderProduct.getOrderQuantity());
            orderProduct.setOrderProductStatus(OrderProductStatus.CANCELED);
        }
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
            Inventory inventory = inventoryRepository.findByProductIdAndColorIdAndSizeId(orderProduct.getProduct().getId(), orderProduct.getColor().getId(), orderProduct.getSize().getId());
            orderDetailProductDTO.setOrderProductId(orderProduct.getId());
            orderDetailProductDTO.setImageUrl(productImage.getImageUrl());
            orderDetailProductDTO.setProductId(orderProduct.getProduct().getId());
            orderDetailProductDTO.setProductName(orderProduct.getProduct().getName());
            orderDetailProductDTO.setOrderQuantity(orderProduct.getOrderQuantity());
            orderDetailProductDTO.setProductPrice(orderProduct.getPrice());
            orderDetailProductDTO.setColorId(orderProduct.getColor().getId());
            orderDetailProductDTO.setColorName(orderProduct.getColor().getName());
            orderDetailProductDTO.setSizeId(orderProduct.getSize().getId());
            orderDetailProductDTO.setSizeName(orderProduct.getSize().getName());
            orderDetailProductDTO.setOrderProductStatus(orderProduct.getOrderProductStatus());
            if (orderProduct.getPrice() * orderProduct.getOrderQuantity() > 30000) {
                orderDetailProductDTO.setDeliveryPrice(0);
            }
            orderDetailProductDTO.setQuantity(inventory.getQuantity());
            return orderDetailProductDTO;
        }).collect(Collectors.toList());

        orderDetail.setOrderDetailProductList(orderDetailProductDTOList);
        return orderDetail;
    }

    private List<OrderProduct> createOrderProductList(List<OrderProductRequestDTO> orderProductRequestDTOList) {
        return orderProductRequestDTOList.stream()
                .map(orderProductRequestDto -> {
                    // 상품, 색상, 사이즈 조회
                    Product product = productRepository.findById(orderProductRequestDto.getProductId()).orElseThrow(EntityNotFoundException::new);
                    Color color = colorRepository.findById(orderProductRequestDto.getColorId()).orElseThrow(EntityNotFoundException::new);
                    Size size = sizeRepository.findById(orderProductRequestDto.getSizeId()).orElseThrow(EntityNotFoundException::new);

                    // 상품 재고 감소
                    Inventory inventory = inventoryRepository.findByProductIdAndColorIdAndSizeId(product.getId(), color.getId(), size.getId());
                    inventory.removeStock(orderProductRequestDto.getOrderQuantity());

                    // 상품의 재고가 0개인 경우 품절 처리
                    List<Inventory> inventories = inventoryRepository.findAllByProductId(product.getId());
                    boolean isOutOfStock = inventories.stream().allMatch(i -> i.getQuantity() == 0);
                    if (isOutOfStock) {
                        product.setProductSellStatus(ProductSellStatus.SOLD_OUT);
                    }

                    // 주문 상품 생성
                    return OrderProduct.createOrderProduct(product, orderProductRequestDto.getOrderQuantity(), color, size);
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
                .map(orderProduct -> cartProductRepository.findByProductIdAndColorIdAndSizeId(orderProduct.getProduct().getId(), orderProduct.getColor().getId(), orderProduct.getSize().getId()))
                .filter(Objects::nonNull)
                .forEach(cartProductRepository::delete);
    }
}
