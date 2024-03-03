package com.windsome.service;

import com.windsome.constant.*;
import com.windsome.dto.order.*;
import com.windsome.entity.*;
import com.windsome.repository.payment.PaymentRepository;
import com.windsome.repository.cartProduct.CartProductRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.productImage.ProductImageRepository;
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

    @Mock private OrderRepository orderRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductRepository productRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private CartProductRepository cartProductRepository;
    @Mock private PaymentRepository paymentRepository;


    @InjectMocks private OrderService orderService;

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
    void testGetOrderProductDetails() {
        // Given
        List<OrderPageProductRequestDTO> orderPageProductRequestDTOList = new ArrayList<>();
        OrderPageProductRequestDTO orderPageProductDto = new OrderPageProductRequestDTO();
        orderPageProductDto.setProductId(1L);
        orderPageProductDto.setCount(1);
        orderPageProductRequestDTOList.add(orderPageProductDto);

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(anyLong())).thenReturn(java.util.Optional.of(product));
        when(productImageRepository.findByProductIdAndIsRepresentativeImage(product.getId(), true)).thenReturn(new ProductImage());

        // When
        List<OrderPageProductResponseDTO> result = orderService.getOrderProductDetails(orderPageProductRequestDTOList);

        // Then
        assertEquals(orderPageProductRequestDTOList.size(), result.size());
    }

    @DisplayName("상품 주문")
    @Test
    void testOrder() {
        // Given
        String userIdentifier = "user1";
        Member member = createMember();

        OrderRequestDTO orderRequestDTO = createSampleOrderDto();
        Product product = createProduct();
        List<OrderProductRequestDTO> orderProductDTOList = getOrderProductDTOList();
        orderRequestDTO.setOrderProductDtoList(orderProductDTOList);

        when(memberRepository.findByUserIdentifier(userIdentifier)).thenReturn(member);
        when(productRepository.findById(anyLong())).thenReturn(java.util.Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderArgument = invocation.getArgument(0);
            // 여기서 실제 데이터베이스에 Order 객체를 저장하고 반환된 Order 객체의 ID를 설정
            orderArgument.setId(1L); // 예시로 ID를 1로 설정
            return orderArgument;
        });
        when(cartProductRepository.findByProductId(1L)).thenReturn(new CartProduct());

        // When
        Long orderId = orderService.order(orderRequestDTO, userIdentifier);

        // Then
        assertNotNull(orderId);
        assertEquals(orderId, 1L);
        assertEquals(member.getAvailablePoints(), 500); // 기존 포인트 0에서 +500

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartProductRepository, times(1)).delete(any(CartProduct.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @DisplayName("주문 취소 권한 검사 - 주문 취소 권한이 있는 경우")
    @Test
    void testVerifyOrderCancellationPermission_Authorized() {
        // Given
        Long orderId = 1L;
        String userIdentifier = "user1";
        Member currentAccount = Member.builder().userIdentifier(userIdentifier).build();
        Member savedAccount = Member.builder().userIdentifier(userIdentifier).build();
        Order order = Order.builder().id(orderId).member(savedAccount).build();

        when(memberRepository.findByUserIdentifier(userIdentifier)).thenReturn(currentAccount);
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        // When
        boolean hasPermission = orderService.verifyOrderCancellationPermission(orderId, userIdentifier);

        // Then
        assertFalse(hasPermission);
        verify(memberRepository, times(1)).findByUserIdentifier(userIdentifier);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @DisplayName("주문 취소 권한 검사 - 주문 취소 권한이 없는 경우")
    @Test
    void testVerifyOrderCancellationPermission_Unauthorized() {
        // Given
        Long orderId = 1L;
        String userIdentifier = "user1";
        Member currentAccount = Member.builder().userIdentifier(userIdentifier).build();
        Member savedAccount = Member.builder().userIdentifier("otherUser").build();
        Order order = Order.builder().id(orderId).member(savedAccount).build();

        when(memberRepository.findByUserIdentifier(userIdentifier)).thenReturn(currentAccount);
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        // When
        boolean hasPermission = orderService.verifyOrderCancellationPermission(orderId, userIdentifier);

        // Then
        assertTrue(hasPermission);
        verify(memberRepository, times(1)).findByUserIdentifier(userIdentifier);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @DisplayName("주문 취소 권한 검사 - 주문이 존재하지 않는 경우")
    @Test
    void testVerifyOrderCancellationPermission_OrderNotFound() {
        // Given
        Long orderId = 1L;
        String userIdentifier = "user1";

        when(memberRepository.findByUserIdentifier(userIdentifier)).thenReturn(new Member());
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

        // When / Then
        assertThrows(EntityNotFoundException.class, () -> {
            orderService.verifyOrderCancellationPermission(orderId, userIdentifier);
        });
        verify(memberRepository, times(1)).findByUserIdentifier(userIdentifier);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @DisplayName("주문 취소")
    @Test
    void testCancelOrder() {
        // Given
        Long orderId = 1L;
        Member member = createMember();
        member.setAvailablePoints(10000);
        member.setTotalUsedPoints(10000);
        member.setTotalEarnedPoints(10000);
        Product product = createProduct();
        Order order = createOrder(member, product);

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        // When
        assertDoesNotThrow(() -> orderService.cancelOrder(orderId));

        // Then
        verify(orderRepository, times(1)).findById(orderId);
        verify(memberRepository, times(1)).save(member);
        assertEquals(0, member.getAvailablePoints());
        assertEquals(10000, member.getTotalUsedPoints());
        assertEquals(0, member.getTotalEarnedPoints());
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
        verify(memberRepository, never()).findById(anyLong());
    }

    private Member createMember() {
        Member member = new Member();
        member.setId(1L);
        member.setName("test");
        member.setUserIdentifier("test1234");
        member.setZipcode("test1");
        member.setAddr("test2");
        member.setAddrDetail("test3");
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
        product.setStockNumber(999);
        product.setDiscount(0);
        return product;
    }

    private Order createOrder(Member member, Product product) {
        Order order = new Order();
        order.setMember(member);
        order.setPrice(10000);
        order.setUsedPoints(0);
        order.setEarnedPoints(10000);

        List<OrderProduct> orderProductList = new ArrayList<>();
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setProduct(product);
        orderProduct.setOrder(order);
        orderProduct.setOrderProductStatus(OrderProductStatus.ORDER);
        orderProduct.setCount(1);
        orderProduct.setPrice(10000);
        orderProductList.add(orderProduct);

        order.setOrderProducts(orderProductList);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PAYMENT_COMPLETED);
        payment.setPrice(10000);
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

    private List<OrderProductRequestDTO> getOrderProductDTOList() {
        List<OrderProductRequestDTO> orderProductDTOList = new ArrayList<>();
        OrderProductRequestDTO orderProductDto = new OrderProductRequestDTO();
        orderProductDto.setId(1L);
        orderProductDto.setCount(1);
        orderProductDto.setPrice(10000);
        orderProductDto.setOrderProductStatus(OrderProductStatus.ORDER);
        orderProductDTOList.add(orderProductDto);
        return orderProductDTOList;
    }

    private OrderRequestDTO createSampleOrderDto() {
        return OrderRequestDTO.builder()
                .orderUid("test")
                .paymentUid("test")
                .name("홍길동")
                .totalOrderPrice(0)
//                .totalDiscountPrice(0)
                .totalPaymentPrice(10000)
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
                .orderProductDtoList(new ArrayList<>())
                .build();
    }

}