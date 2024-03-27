package com.windsome.service;

import com.windsome.constant.*;
import com.windsome.dto.order.*;
import com.windsome.entity.cart.CartProduct;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.entity.order.OrderProduct;
import com.windsome.entity.order.Payment;
import com.windsome.entity.product.Product;
import com.windsome.entity.product.ProductImage;
import com.windsome.entity.product.ProductOption;
import com.windsome.repository.order.OrderRepository;
import com.windsome.service.cart.CartProductService;
import com.windsome.service.member.MemberService;
import com.windsome.service.order.OrderService;
import com.windsome.service.order.PaymentService;
import com.windsome.service.product.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks private OrderService orderService;

    @Mock OrderRepository orderRepository;
    @Mock ProductService productService;
    @Mock MemberService memberService;
    @Mock ProductImageService productImageService;
    @Mock CartProductService cartProductService;
    @Mock PaymentService paymentService;
    @Mock ProductOptionService productOptionService;

    @Test
    @DisplayName("주문 조회 - 주문이 존재하는 경우")
    void testGetOrderList_OrderExists() {
        // Given
        String userIdentifier = "user123";
        Pageable pageable = mock(Pageable.class);

        List<Order> orders = new ArrayList<>();
        List<OrderProduct> orderProductList = new ArrayList<>();
        Order order = createOrder(orderProductList);
        orders.add(order);

        when(orderRepository.findOrders(userIdentifier, pageable)).thenReturn(orders);
        when(orderRepository.countOrder(userIdentifier)).thenReturn((long) orders.size());

        // When
        Page<OrderHistResponseDTO> resultPage = orderService.getOrderList(userIdentifier, pageable);

        // Then
        assertEquals(orders.size(), resultPage.getContent().size());
        assertEquals(orders.size(), resultPage.getTotalElements());
    }

    @Test
    @DisplayName("주문 조회 - 주문이 없는 경우")
    void testGetOrderList_NoOrderExists() {
        // Given
        String userIdentifier = "user123";
        Pageable pageable = mock(Pageable.class);

        List<Order> orders = new ArrayList<>(); // Empty list

        when(orderRepository.findOrders(userIdentifier, pageable)).thenReturn(orders);
        when(orderRepository.countOrder(userIdentifier)).thenReturn((long) orders.size());

        // When
        Page<OrderHistResponseDTO> resultPage = orderService.getOrderList(userIdentifier, pageable);

        // Then
        assertTrue(resultPage.getContent().isEmpty());
        assertEquals(orders.size(), resultPage.getTotalElements());
    }

    @DisplayName("주문서 작성 페이지 - 주문 상품 정보")
    @Test
    void testGetOrderProductsInfo() {
        // Given
        Product product = createProduct();
        ProductImage productImage = new ProductImage();
        productImage.setImageUrl("url");

        CartProduct cartProduct = new CartProduct();
        cartProduct.setId(1L);

        OrderProductListDTO orderProductListDTO = createOrderProductListDTO();
        OrderProductDTO orderProductDTO = new OrderProductDTO();
        orderProductDTO.setProductId(product.getId());
        orderProductDTO.setColor("블랙");
        orderProductDTO.setSize("M");
        orderProductDTO.setOrderQuantity(100);

        when(productService.getProductByProductId(anyLong())).thenReturn(product);
        when(productImageService.getRepresentativeImageUrl(anyLong(), anyBoolean())).thenReturn(productImage.getImageUrl());
        when(cartProductService.getCartProductByProductIdAndColorAndSize(anyLong(), any(), any())).thenReturn(cartProduct);

        // When
        List<OrderProductResponseDTO> orderProductsInfo = orderService.getOrderProductsInfo(orderProductListDTO);

        // Then
        assertEquals(orderProductsInfo.size(), orderProductListDTO.getOrderProducts().size());
    }

    @DisplayName("상품 주문")
    @Test
    void testOrder() {
        // Given
        OrderRequestDTO orderRequestDTO = createOrderRequestDTO();
        orderRequestDTO.setEarnedPoints(900);
        orderRequestDTO.setUsedPoints(1000);

        Long memberId = 1L;
        Member member = createMember(); member.setAvailablePoints(1000); member.setTotalEarnedPoints(1000);
        Product product = createProduct();

        List<ProductOption> productOptionList = new ArrayList<>();
        ProductOption productOption = createProductOption(product);
        productOptionList.add(productOption);

        when(memberService.getMemberByMemberId(memberId)).thenReturn(member);
        when(productService.getProductByProductId(anyLong())).thenReturn(product);
        when(productOptionService.getProductOptionByProductIdAndColorAndSize(product.getId(), "블랙", "M")).thenReturn(productOption);
        when(productOptionService.getProductOptionsByProductId(product.getId())).thenReturn(productOptionList);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderArgument = invocation.getArgument(0);
            // 여기서 실제 데이터베이스에 Order 객체를 저장하고 반환된 Order 객체의 ID를 설정
            orderArgument.setId(1L); // 예시로 ID를 1로 설정
            return orderArgument;
        });
        when(cartProductService.getCartProductByProductIdAndColorAndSize(1L, "블랙", "M")).thenReturn(new CartProduct());

        // When
        Long orderId = orderService.order(orderRequestDTO, memberId);

        // Then
        assertNotNull(orderId);
        assertEquals(orderId, 1L);
        assertEquals(member.getAvailablePoints(), 900); // 기존 0 포인트 + 900 포인트
        assertEquals(member.getTotalEarnedPoints(), 1900); // 기존 1000 포인트 + 900 포인트
        assertEquals(member.getTotalUsedPoints(), 1000); // 총 사용 포인트
        assertEquals(productOption.getQuantity(), 99); // 상품 재고 감소 확인

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartProductService, times(1)).deleteCartProduct(any(CartProduct.class));
        verify(productOptionService, times(1)).getProductOptionByProductIdAndColorAndSize(product.getId(), "블랙", "M");
        verify(productOptionService, times(1)).getProductOptionsByProductId(product.getId());
        verify(paymentService, times(1)).savePayment(any(Payment.class));
    }

    @NotNull
    private static ProductOption createProductOption(Product product) {
        ProductOption productOption = new ProductOption();
        productOption.setProduct(product);
        productOption.setColor("블랙");
        productOption.setSize("M");
        productOption.setQuantity(100);
        return productOption;
    }

    @DisplayName("주문 취소 권한 검사 - 주문 취소 권한이 있는 경우")
    @Test
    void testVerifyOrderCancellationPermission_Authorized() {
        // Given
        Long orderId = 1L;
        Member member = Member.builder().id(1L).build();
        Order order = Order.builder().id(orderId).member(member).build();

        when(memberService.getMemberByMemberId(member.getId())).thenReturn(member);
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        // When
        boolean hasPermission = orderService.verifyOrderCancellationPermission(orderId, member.getId());

        // Then
        assertFalse(hasPermission);
        verify(memberService, times(1)).getMemberByMemberId(member.getId());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @DisplayName("주문 취소 권한 검사 - 주문 취소 권한이 없는 경우")
    @Test
    void testVerifyOrderCancellationPermission_Unauthorized() {
        // Given
        Long orderId = 1L;
        Member currentAccount = Member.builder().id(1L).userIdentifier("currentAccount").build();
        Member orderAccount = Member.builder().id(2L).userIdentifier("orderAccount").build();
        Order order = Order.builder().id(orderId).member(orderAccount).build();

        when(memberService.getMemberByMemberId(currentAccount.getId())).thenReturn(currentAccount);
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        // When
        boolean hasPermission = orderService.verifyOrderCancellationPermission(orderId, currentAccount.getId());

        // Then
        assertTrue(hasPermission);
        verify(memberService, times(1)).getMemberByMemberId(currentAccount.getId());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @DisplayName("주문 취소 권한 검사 - 주문이 존재하지 않는 경우")
    @Test
    void testVerifyOrderCancellationPermission_OrderNotFound() {
        // Given
        Long orderId = 1L;
        Long memberId = 1L;

        when(memberService.getMemberByMemberId(memberId)).thenReturn(new Member());
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

        // When / Then
        assertThrows(EntityNotFoundException.class, () -> {
            orderService.verifyOrderCancellationPermission(orderId, memberId);
        });
        verify(memberService, times(1)).getMemberByMemberId(memberId);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @DisplayName("주문 취소")
    @Test
    void testCancelOrder() {
        // Given
        Long orderId = 1L;
        Member member = createMember(); member.setAvailablePoints(1000); member.setTotalEarnedPoints(2000); member.setTotalUsedPoints(1000);
        Product product = createProduct();
        Order order = createOrder(member, product);
        ProductOption productOption = createProductOption(product);

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
        when(productOptionService.getProductOptionByProductIdAndColorAndSize(anyLong(), any(), any())).thenReturn(productOption);

        // When
        assertDoesNotThrow(() -> orderService.cancelOrder(orderId));

        // Then
        verify(orderRepository, times(1)).findById(orderId);
        verify(memberService, times(1)).saveMember(member);
        assertEquals(1100, member.getAvailablePoints()); // 1000(기존 포인트) + 1000(사용 포인트 복구) - 900(적립 포인트 회수) = 1100
        assertEquals(0, member.getTotalUsedPoints());
        assertEquals(1100, member.getTotalEarnedPoints());
        assertEquals(order.getOrderStatus(), OrderStatus.CANCELED);
        assertEquals(order.getPayment().getStatus(), PaymentStatus.PAYMENT_CANCELLED);
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            assertEquals(orderProduct.getOrderProductStatus(), OrderProductStatus.CANCELED);
        }
        assertEquals(productOption.getQuantity(), 101); // 재고 복구 확인
    }

    @DisplayName("존재하지 않는 주문 취소")
    @Test
    void testCancelNonExistingOrder() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

        // When, Then
        assertThrows(EntityNotFoundException.class, () -> orderService.cancelOrder(orderId));
        verify(orderRepository, times(1)).findById(orderId);
        verify(memberService, never()).getMemberByMemberId(anyLong());
    }

    private Member createMember() {
        Member member = new Member();
        member.setId(1L);
        member.setName("test");
        member.setUserIdentifier("test1234");
        member.setAvailablePoints(0);
        member.setTotalUsedPoints(0);
        member.setTotalEarnedPoints(0);
        member.setRole(Role.USER);
        return member;
    }

    private Product createProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Name");
        product.setProductDetail("Test Detail");
        product.setProductSellStatus(ProductSellStatus.AVAILABLE);
        product.setPrice(10000);
        product.setDiscount(0);
        return product;
    }

    private Order createOrder(Member member, Product product) {
        Order order = new Order();
        order.setMember(member);
        order.setPrice(30000);
        order.setUsedPoints(1000);
        order.setEarnedPoints(900);

        List<OrderProduct> orderProductList = new ArrayList<>();
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setProduct(product);
        orderProduct.setColor("블랙");
        orderProduct.setSize("M");
        orderProduct.setOrder(order);
        orderProduct.setOrderProductStatus(OrderProductStatus.ORDER);
        orderProduct.setOrderQuantity(1);
        orderProduct.setPrice(30000);
        orderProductList.add(orderProduct);

        order.setOrderProducts(orderProductList);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PAYMENT_COMPLETED);
        payment.setPrice(29000);
        payment.setPaymentUid("test");
        order.setPayment(payment);
        return order;
    }

    private Order createOrder(List<OrderProduct> orderProductList) {
        return Order.builder()
                .id(1L)
                .member(new Member())
                .orderDate(LocalDateTime.now())
                .price(10000)
                .usedPoints(1000)
                .zipcode("주소1")
                .addr("주소2")
                .addrDetail("주소3")
                .tel("010-1234-5678")
                .email("test@example.com")
                .req("배송 요청 사항")
                .orderStatus(OrderStatus.PROCESSING)
                .orderProducts(orderProductList)
                .build();
    }

    private OrderRequestDTO createOrderRequestDTO() {
        List<OrderProductRequestDTO> orderProductRequestDTOList = new ArrayList<>();
        OrderProductRequestDTO orderProductRequestDTO = new OrderProductRequestDTO();
        orderProductRequestDTO.setProductId(1L);
        orderProductRequestDTO.setColor("블랙");
        orderProductRequestDTO.setSize("M");
        orderProductRequestDTO.setPrice(30000);
        orderProductRequestDTO.setOrderQuantity(1);
        orderProductRequestDTOList.add(orderProductRequestDTO);

        return OrderRequestDTO.builder()
                .orderUid("test")
                .paymentUid("test")
                .name("홍길동")
                .totalProductPrice(30000)
                .totalOrderPrice(30000)
                .totalPaymentPrice(29000)
                .zipcode("123 Main Street")
                .addr("Apartment 101")
                .addrDetail("Cityville")
                .tel("123-456-7890")
                .email("test@example.com")
                .req("긴급 배송 요청: 문 앞에 놓아주세요")
                .earnedPoints(0)
                .usedPoints(0)
                .productCount(1)
                .repProductName("대표 상품 이름")
                .repProductImage("url")
                .orderProductDtoList(orderProductRequestDTOList)
                .build();
    }

    private OrderProductListDTO createOrderProductListDTO() {
        OrderProductListDTO orderProductListDTO = new OrderProductListDTO();
        List<OrderProductDTO> orderProductDTOList = new ArrayList<>();

        for (long i = 1; i <= 3; i++) {
            OrderProductDTO orderProductDTO = new OrderProductDTO();
            orderProductDTO.setProductId(i);
            orderProductDTO.setColor("블랙" + i);
            orderProductDTO.setSize("M" + i);
            orderProductDTO.setOrderQuantity(1);
            orderProductDTOList.add(orderProductDTO);
        }

        orderProductListDTO.setOrderProducts(orderProductDTOList);
        return orderProductListDTO;
    }

}