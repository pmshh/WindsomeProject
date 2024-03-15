package com.windsome.service;

import com.windsome.constant.*;
import com.windsome.dto.order.*;
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
import com.windsome.repository.product.ColorRepository;
import com.windsome.repository.product.InventoryRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.product.SizeRepository;
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
import java.util.Optional;

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
    @Mock private ColorRepository colorRepository;
    @Mock private SizeRepository sizeRepository;
    @Mock private InventoryRepository inventoryRepository;

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
    void testGetOrderProductsInfo() {
        // Given
        OrderProductListDTO orderProductListDTO = createOrderProductListDTO();
        Product product = createProduct();
        ProductImage productImage = new ProductImage();
        productImage.setImageUrl("url");
        CartProduct cartProduct = new CartProduct();
        cartProduct.setId(1L);

        when(productRepository.findById(anyLong())).thenReturn(java.util.Optional.of(product));
        when(productImageRepository.findByProductIdAndIsRepresentativeImage(anyLong(), anyBoolean())).thenReturn(productImage);
        when(cartProductRepository.findByProductIdAndColorIdAndSizeId(anyLong(), anyLong(), anyLong())).thenReturn(cartProduct);

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
        String userIdentifier = "test";

        Member member = createMember(); member.setAvailablePoints(1000); member.setTotalEarnedPoints(1000);
        when(memberRepository.findByUserIdentifier(userIdentifier)).thenReturn(member);

        Product product = createProduct();
        when(productRepository.findById(anyLong())).thenReturn(java.util.Optional.of(product));

        Color color = new Color(); color.setId(1L);
        when(colorRepository.findById(anyLong())).thenReturn(Optional.of(color));

        Size size = new Size(); size.setId(1L);
        when(sizeRepository.findById(anyLong())).thenReturn(Optional.of(size));

        Inventory inventory = new Inventory(); inventory.setQuantity(2);
        when(inventoryRepository.findByProductIdAndColorIdAndSizeId(product.getId(), color.getId(), size.getId())).thenReturn(inventory);

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
        assertEquals(member.getAvailablePoints(), 900); // 기존 0 포인트 + 900 포인트
        assertEquals(member.getTotalEarnedPoints(), 1900); // 기존 1000 포인트 + 900 포인트
        assertEquals(member.getTotalUsedPoints(), 1000); // 총 사용 포인트
        assertEquals(inventory.getQuantity(), 1); // 상품 재고 감소 확인

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartProductRepository, times(1)).delete(any(CartProduct.class));
        verify(colorRepository, times(1)).findById(anyLong());
        verify(sizeRepository, times(1)).findById(anyLong());
        verify(inventoryRepository, times(1)).findByProductIdAndColorIdAndSizeId(anyLong(),anyLong(),anyLong());
        verify(inventoryRepository, times(1)).findAllByProductId(anyLong());
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
        Member member = createMember(); member.setAvailablePoints(1000); member.setTotalEarnedPoints(2000); member.setTotalUsedPoints(1000);
        Product product = createProduct();
        Order order = createOrder(member, product);
        Inventory inventory = new Inventory(); inventory.setQuantity(2);

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
        when(inventoryRepository.findByProductIdAndColorIdAndSizeId(anyLong(), anyLong(), anyLong())).thenReturn(inventory);

        // When
        assertDoesNotThrow(() -> orderService.cancelOrder(orderId));

        // Then
        verify(orderRepository, times(1)).findById(orderId);
        verify(memberRepository, times(1)).save(member);
        assertEquals(1100, member.getAvailablePoints()); // 1000(기존 포인트) + 1000(사용 포인트 복구) - 900(적립 포인트 회수) = 1100
        assertEquals(0, member.getTotalUsedPoints());
        assertEquals(1100, member.getTotalEarnedPoints());
        assertEquals(order.getOrderStatus(), OrderStatus.REFUNDED);
        assertEquals(order.getPayment().getStatus(), PaymentStatus.PAYMENT_CANCELLED);
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            assertEquals(orderProduct.getOrderProductStatus(), OrderProductStatus.CANCELED);
        }
        assertEquals(inventory.getQuantity(), 3); // 재고 복구 확인
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
        product.setDiscount(0);
        return product;
    }

    private Order createOrder(Member member, Product product) {
        Order order = new Order();
        order.setMember(member);
        order.setPrice(30000);
        order.setUsedPoints(1000);
        order.setEarnedPoints(900);

        Color color = new Color();
        color.setId(1L);

        Size size = new Size();
        size.setId(1L);

        List<OrderProduct> orderProductList = new ArrayList<>();
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setProduct(product);
        orderProduct.setColor(color);
        orderProduct.setSize(size);
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
        orderProductRequestDTO.setColorId(1L);
        orderProductRequestDTO.setSizeId(1L);
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
            orderProductDTO.setColorId(i);
            orderProductDTO.setColorName("Color " + i);
            orderProductDTO.setSizeId(i);
            orderProductDTO.setSizeName("Size " + i);
            orderProductDTO.setOrderQuantity(1);
            orderProductDTOList.add(orderProductDTO);
        }

        orderProductListDTO.setOrderProducts(orderProductDTOList);
        return orderProductListDTO;
    }

}