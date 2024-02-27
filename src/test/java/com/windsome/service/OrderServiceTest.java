package com.windsome.service;

import com.windsome.constant.OrderProductStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.constant.ProductSellStatus;
import com.windsome.dto.order.OrderDto;
import com.windsome.dto.order.OrderListDto;
import com.windsome.dto.order.OrderPageProductDto;
import com.windsome.dto.order.OrderProductDto;
import com.windsome.entity.*;
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
        Page<OrderListDto> resultPage = orderService.getOrderList(userIdentifier, pageable);

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
        Page<OrderListDto> resultPage = orderService.getOrderList(userIdentifier, pageable);

        // Then
        assertTrue(resultPage.getContent().isEmpty());
        assertEquals(orders.size(), resultPage.getTotalElements());
    }

    @DisplayName("주문서 작성 페이지 - 주문 상품 정보")
    @Test
    void testGetOrderProductDetails() {
        // Given
        List<OrderPageProductDto> orders = new ArrayList<>();
        OrderPageProductDto orderPageProductDto = new OrderPageProductDto();
        orderPageProductDto.setProductId(1L);
        orderPageProductDto.setImageUrl("test url");
        orderPageProductDto.setPoint(0);
        orderPageProductDto.setTotalPoint(0);
        orderPageProductDto.setTotalPrice(0);
        orders.add(orderPageProductDto);

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(anyLong())).thenReturn(java.util.Optional.of(product));
        when(productImageRepository.findByProductIdAndIsRepresentativeImage(product.getId(), true)).thenReturn(new ProductImage());

        // When
        List<OrderPageProductDto> result = orderService.getOrderProductDetails(orders);

        // Then
        assertEquals(orders.size(), result.size());
    }

    @DisplayName("상품 주문")
    @Test
    void testOrder() {
        // Given
        String userIdentifier = "user1";
        Member member = createMember();

        OrderDto orderDto = createSampleOrderDto();
        Product product = createProduct();
        List<OrderProductDto> orderProductDtoList = getOrderProductDtoList();
        orderDto.setOrders(orderProductDtoList);

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
        Long orderId = orderService.order(orderDto, userIdentifier);

        // Then
        assertNotNull(orderId);
        assertEquals(orderId, 1L);
        assertEquals(member.getPoint(), 500); // 기존 포인트 0에서 +500
        assertEquals(orderDto.getOrderFinalSalePrice(), 12500); // 상품 가격 10,000원 + 배송비 2,500원 = 12,500원

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartProductRepository, times(1)).delete(any(CartProduct.class));
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
        assertTrue(hasPermission);
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
        assertFalse(hasPermission);
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
        member.setPoint(500);
        member.setTotalPoint(500);
        member.setTotalOrderPrice(12500);
        Product product = createProduct();
        Order order = createOrder(member, product);

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        // When
        assertDoesNotThrow(() -> orderService.cancelOrder(orderId));

        // Then
        verify(orderRepository, times(1)).findById(orderId);
        verify(memberRepository, times(1)).save(member);
        assertEquals(0, member.getPoint()); // 회원 포인트가 복구되었는지 확인
        assertEquals(0, member.getTotalOrderPrice()); // 총 주문 금액이 복구되었는지 확인
        assertEquals(0, member.getTotalUsePoint()); // 총 사용 포인트가 복구되었는지 확인
        assertEquals(0, member.getTotalPoint()); // 총 적립 포인트가 복구되었는지 확인
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
        member.setAddress1("test1");
        member.setAddress2("test2");
        member.setAddress3("test3");
        member.setPoint(0);
        member.setTotalPoint(0);
        member.setTotalOrderPrice(0);
        member.setTotalUsePoint(0);
        return member;
    }

    private Product createProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Name");
        product.setProductDetail("Test Detail");
        product.setProductSellStatus(ProductSellStatus.SELL);
        product.setPrice(10000);
        product.setStockNumber(999);
        product.setDiscount(0);
        return product;
    }

    private Order createOrder(Member member, Product product) {
        Order order = new Order();
        order.setMember(member);
        order.setDeliveryCost(2500);
        order.setTotalOrderPrice(12500);

        List<OrderProduct> orderProductList = new ArrayList<>();
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setProduct(product);
        orderProduct.setOrder(order);
        orderProduct.setOrderProductStatus(OrderProductStatus.ORDER);
        orderProduct.setCount(1);
        orderProduct.setPrice(10000);
        orderProduct.setDiscount(0);
        orderProduct.setAccumulatedPoints(500);
        orderProductList.add(orderProduct);

        order.setOrderProducts(orderProductList);
        return order;
    }

    private Order createOrder(List<OrderProduct> orderProductList) {
        return Order.builder()
                .id(1L)
                .member(new Member())
                .orderDate(LocalDateTime.now())
                .totalOrderPrice(10000)
                .deliveryCost(2000)
                .usePoint(1000)
                .address1("주소1")
                .address2("주소2")
                .address3("주소3")
                .tel("010-1234-5678")
                .email("test@example.com")
                .req("배송 요청 사항")
                .orderStatus(OrderStatus.READY)
                .orderProducts(orderProductList)
                .build();
    }

    private List<OrderProductDto> getOrderProductDtoList() {
        List<OrderProductDto> orderProductDtoList = new ArrayList<>();
        OrderProductDto orderProductDto = new OrderProductDto();
        orderProductDto.setProductId(1L);
        orderProductDto.setCount(1);
        orderProductDto.setPrice(10000);
        orderProductDto.setAccumulatedPoints(500);
        orderProductDto.setOrderProductStatus(OrderProductStatus.ORDER);
        orderProductDto.setDiscount(0);
        orderProductDto.setSalePrice(10000);
        orderProductDto.setTotalSalePrice(10000);
        orderProductDto.setTotalSavePoint(500);
        orderProductDtoList.add(orderProductDto);
        return orderProductDtoList;
    }

    private OrderDto createSampleOrderDto() {
        return OrderDto.builder()
                .address1("123 Main Street")
                .address2("Apartment 101")
                .address3("Cityville")
                .tel("123-456-7890")
                .email("test@example.com")
                .req("긴급 배송 요청: 문 앞에 놓아주세요")
                .orders(new ArrayList<>())
                .deliveryCost(2500)
                .usePoint(0)
                .build();
    }

}