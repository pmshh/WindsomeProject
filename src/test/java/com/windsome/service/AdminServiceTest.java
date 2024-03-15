package com.windsome.service;

import com.windsome.constant.Role;
import com.windsome.dto.admin.CategorySalesResult;
import com.windsome.dto.admin.DashboardInfoDto;
import com.windsome.dto.admin.OrderManagementDTO;
import com.windsome.dto.member.AdminMemberDetailDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.entity.product.Product;
import com.windsome.exception.AdminDeletionException;
import com.windsome.repository.board.qa.QaRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.repository.orderProduct.OrderProductRepository;
import com.windsome.repository.payment.PaymentRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.productImage.ProductImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.windsome.TestUtil.createMember;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class AdminServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private ProductRepository productRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderProductRepository orderProductRepository;
    @Mock private QaRepository qaRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AdminService adminService;

    @Test
    @DisplayName("대시보드 데이터 조회")
    public void testGetDashboardData() {
        // Given
        when(memberRepository.count()).thenReturn(10L);
        when(productRepository.count()).thenReturn(20L);
        when(qaRepository.count()).thenReturn(5L);
        when(paymentRepository.getTotalPaymentPrice()).thenReturn(500L);

        CategorySalesResult categorySalesResult1 = mock(CategorySalesResult.class);
        when(categorySalesResult1.getCategory()).thenReturn(1L);
        when(categorySalesResult1.getOrderQuantity()).thenReturn(100L);

        CategorySalesResult categorySalesResult2 = mock(CategorySalesResult.class);
        when(categorySalesResult2.getCategory()).thenReturn(2L);
        when(categorySalesResult2.getOrderQuantity()).thenReturn(200L);

        List<CategorySalesResult> categorySalesResults = Arrays.asList(categorySalesResult1, categorySalesResult2);
        when(orderProductRepository.getCategorySalesCount()).thenReturn(categorySalesResults);

        // When
        DashboardInfoDto dashboardInfoDto = adminService.getDashboardData();

        // Then
        verify(memberRepository, times(1)).count();
        verify(productRepository, times(1)).count();
        verify(qaRepository, times(1)).count();
        verify(paymentRepository, times(1)).getTotalPaymentPrice();
        verify(orderProductRepository, times(1)).getCategorySalesCount();

        assertEquals(10L, dashboardInfoDto.getTotalMembers());
        assertEquals(20L, dashboardInfoDto.getTotalProducts());
        assertEquals(5L, dashboardInfoDto.getTotalQaPosts());
        assertEquals(500L, dashboardInfoDto.getTotalOrderPrice());
        assertEquals(2, dashboardInfoDto.getCategorySalesList().size());
        assertEquals(1L, dashboardInfoDto.getCategorySalesList().get(0).getCategory());
        assertEquals(100L, dashboardInfoDto.getCategorySalesList().get(0).getOrderQuantity());
        assertEquals(2L, dashboardInfoDto.getCategorySalesList().get(1).getCategory());
        assertEquals(200L, dashboardInfoDto.getCategorySalesList().get(1).getOrderQuantity());
    }

    @Test
    @DisplayName("회원 상세 정보 조회")
    public void testGetMemberDetails() {
        // Given
        Long accountId = 1L;
        Member member = createMember(accountId);
        when(memberRepository.findById(accountId)).thenReturn(java.util.Optional.of(member));

        // When
        AdminMemberDetailDTO result = adminService.getMemberDetails(accountId);

        // Then
        verify(memberRepository, times(1)).findById(accountId);
        assertEquals(member.getId(), result.getId());
        assertEquals(member.getUserIdentifier(), result.getUserIdentifier());
        assertEquals(member.getPassword(), result.getPassword());
        assertEquals(member.getName(), result.getName());
        assertEquals(member.getEmail(), result.getEmail());
        assertEquals(member.getZipcode(), result.getZipcode());
        assertEquals(member.getAddr(), result.getAddr());
        assertEquals(member.getAddrDetail(), result.getAddrDetail());
        assertEquals(member.getAvailablePoints(), result.getAvailablePoints());
        assertEquals(member.getTotalUsedPoints(), result.getTotalUsedPoints());
        assertEquals(member.getTotalEarnedPoints(), result.getTotalEarnedPoints());
    }

    @Test
    @DisplayName("회원 상세 정보 조회 시 EntityNotFoundException 발생")
    public void testGetMemberDetails_ThrowsEntityNotFoundException() {
        // Given
        Long accountId = 1L;
        when(memberRepository.findById(accountId)).thenReturn(java.util.Optional.empty());

        // When
        Executable executable = () -> adminService.getMemberDetails(accountId);

        // Then
        assertThrows(EntityNotFoundException.class, executable);
        verify(memberRepository, times(1)).findById(accountId);
    }

    @Test
    @DisplayName("회원 상세 정보 업데이트")
    public void testUpdateMemberDetails() throws Exception {
        // Given
        AdminMemberDetailDTO dto = AdminMemberDetailDTO.builder()
                .id(1L)
                .userIdentifier("user1")
                .password("newpassword")
                .name("John Doe")
                .email("john.doe@example.com")
                .zipcode("Address1")
                .addr("Address2")
                .addrDetail("Address3")
                .availablePoints(100)
                .totalUsedPoints(1000)
                .totalEarnedPoints(500)
                .build();

        Member member = new Member();
        member.setId(dto.getId());
        member.setUserIdentifier(dto.getUserIdentifier());
        member.setPassword(dto.getPassword());
        member.setName(dto.getName());
        member.setEmail(dto.getEmail());
        member.setZipcode(dto.getZipcode());
        member.setAddr(dto.getAddr());
        member.setAddrDetail(dto.getAddrDetail());
        member.setAvailablePoints(dto.getAvailablePoints());
        member.setTotalUsedPoints(dto.getTotalUsedPoints());
        member.setTotalEarnedPoints(dto.getTotalEarnedPoints());

        when(memberRepository.findById(dto.getId())).thenReturn(java.util.Optional.of(member));
        when(passwordEncoder.encode("newpassword")).thenReturn("$2a$10$gLKb.8YwrDpQVmbZpRiMzOaEmI6oUxgWDEO75nKoqyQKOWoBvC.Ci");

        // When
        adminService.updateMember(dto);

        // Then
        verify(memberRepository, times(1)).findById(dto.getId());
        verify(modelMapper, times(1)).map(dto, member);
        verify(passwordEncoder, times(1)).encode(dto.getPassword());
        verify(memberRepository, times(1)).save(member);

        // Verify that the member was updated correctly
        assertEquals(dto.getId(), member.getId());
        assertEquals(dto.getUserIdentifier(), member.getUserIdentifier());
        assertEquals("$2a$10$gLKb.8YwrDpQVmbZpRiMzOaEmI6oUxgWDEO75nKoqyQKOWoBvC.Ci", member.getPassword());
        assertEquals(dto.getName(), member.getName());
        assertEquals(dto.getEmail(), member.getEmail());
        assertEquals(dto.getZipcode(), member.getZipcode());
        assertEquals(dto.getAddr(), member.getAddr());
        assertEquals(dto.getAddrDetail(), member.getAddrDetail());
        assertEquals(dto.getAvailablePoints(), member.getAvailablePoints());
        assertEquals(dto.getTotalUsedPoints(), member.getTotalUsedPoints());
        assertEquals(dto.getTotalEarnedPoints(), member.getTotalEarnedPoints());
    }

    @Test
    @DisplayName("회원 상세 정보 업데이트 시 EntityNotFoundException 발생")
    public void testUpdateMemberDetails_EntityNotFoundException() {
        // Given
        AdminMemberDetailDTO dto = AdminMemberDetailDTO.builder()
                .id(1L)
                .build();

        when(memberRepository.findById(dto.getId())).thenReturn(java.util.Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> adminService.updateMember(dto));
        verify(memberRepository, times(1)).findById(dto.getId());
        verifyNoMoreInteractions(modelMapper, passwordEncoder, memberRepository);
    }

    @Test
    @DisplayName("회원 권한 업데이트")
    void testUpdateMemberRole() {
        // Given
        Long accountId = 1L;
        Role newRole = Role.ADMIN;

        Member existingMember = new Member();
        existingMember.setId(accountId);
        existingMember.setRole(Role.USER);

        when(memberRepository.findById(accountId)).thenReturn(Optional.of(existingMember));

        // When
        adminService.updateMemberRole(accountId, newRole);

        // Then
        verify(memberRepository, times(1)).findById(accountId);
        verify(memberRepository, times(1)).save(existingMember);

        assertEquals(newRole, existingMember.getRole());
    }

    @Test
    @DisplayName("회원 권한 업데이트 시 EntityNotFoundException 발생")
    void testUpdateMemberRole_EntityNotFoundException() {
        // Given
        Long accountId = 1L;
        Role newRole = Role.ADMIN;

        when(memberRepository.findById(accountId)).thenReturn(Optional.empty());

        // When, Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            adminService.updateMemberRole(accountId, newRole);
        });

        assertEquals(EntityNotFoundException.class, exception.getClass());
        verify(memberRepository, times(1)).findById(accountId);
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원 삭제 - 회원이 존재하는 경우")
    void testDeleteMember() throws AdminDeletionException {
        // Given
        Long accountId = 1L;
        Long[] accountIds = {1L};
        Member member = new Member();
        member.setRole(Role.USER);

        when(memberRepository.findById(accountId)).thenReturn(java.util.Optional.of(member));

        // When
        adminService.deleteMembers(accountIds);

        // Then
        verify(memberRepository, times(1)).findById(accountId);
        verify(memberRepository, times(1)).delete(member);
    }

    @Test
    @DisplayName("회원 삭제 - 회원이 관리자인 경우")
    void testDeleteMember_AdminRole() {
        // Given
        Long accountId = 1L;
        Long[] accountIds = {1L};
        Member adminMember = new Member();
        adminMember.setRole(Role.ADMIN);

        when(memberRepository.findById(accountId)).thenReturn(java.util.Optional.of(adminMember));

        // When, Then
        AdminDeletionException exception = assertThrows(AdminDeletionException.class, () -> {
            adminService.deleteMembers(accountIds);
        });

        verify(memberRepository, times(1)).findById(accountId);
        verify(memberRepository, never()).delete(any());
    }

    @Test
    @DisplayName("회원 삭제 - 회원이 존재하지 않는 경우")
    void testDeleteMember_EntityNotFoundException() {
        // Given
        Long accountId = 1L;
        Long[] accountIds = {1L};
        when(memberRepository.findById(accountId)).thenReturn(java.util.Optional.empty());

        // When, Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            adminService.deleteMembers(accountIds);
        });

        verify(memberRepository, times(1)).findById(accountId);
        verify(memberRepository, never()).delete(any());
    }

    @Test
    @DisplayName("주문 조회 - 주문이 존재하는 경우")
    void testGetOrderList_OrdersExist() {
        // Given
        String userIdentifier = "user1";
        Pageable pageable = mock(Pageable.class);

        Member member = createMember(1L);

        List<Order> orders = new ArrayList<>();
        Order order1 = new Order();
        order1.setOrderDate(LocalDateTime.now());
        order1.setMember(member);
        orders.add(order1);
        Order order2 = new Order();
        order2.setOrderDate(LocalDateTime.now());
        order2.setMember(member);
        orders.add(order2);

        when(orderRepository.findOrderListForAdmin(userIdentifier, pageable)).thenReturn(orders);
        when(orderRepository.countOrderList(userIdentifier)).thenReturn(2L);

        // When
        Page<OrderManagementDTO> resultPage = adminService.getOrderList(userIdentifier, pageable);

        // Then
        assertEquals(2, resultPage.getContent().size());
        assertEquals(2L, resultPage.getTotalElements());
        verify(orderRepository, times(1)).findOrderListForAdmin(userIdentifier, pageable);
        verify(orderRepository, times(1)).countOrderList(userIdentifier);
    }

    @Test
    @DisplayName("주문 조회 - 주문이 존재하지 않는 경우")
    void testGetOrderList_NoOrders() {
        // Given
        String userIdentifier = "user123";
        Pageable pageable = mock(Pageable.class);

        List<Order> orders = new ArrayList<>();

        when(orderRepository.findOrderListForAdmin(userIdentifier, pageable)).thenReturn(orders);
        when(orderRepository.countOrderList(userIdentifier)).thenReturn(0L);

        // When
        Page<OrderManagementDTO> resultPage = adminService.getOrderList(userIdentifier, pageable);

        // Then
        assertEquals(0, resultPage.getContent().size());
        assertEquals(0L, resultPage.getTotalElements());
        verify(orderRepository, times(1)).findOrderListForAdmin(userIdentifier, pageable);
        verify(orderRepository, times(1)).countOrderList(userIdentifier);
    }

    @Test
    @DisplayName("상품 조회 - 상품이 존재하는 경우")
    void testGetProductList_ProductsExist() {
        // Given
        ProductSearchDTO productSearchDto = new ProductSearchDTO();
        Pageable pageable = mock(Pageable.class);

        List<Product> products = new ArrayList<>();
        products.add(new Product());
        products.add(new Product());

        when(productRepository.findProducts(productSearchDto, pageable)).thenReturn(new PageImpl<>(products));

        // When
        Page<Product> resultPage = adminService.getProductList(productSearchDto, pageable);

        // Then
        assertEquals(2, resultPage.getContent().size());
        verify(productRepository, times(1)).findProducts(productSearchDto, pageable);
    }

    @Test
    @DisplayName("상품 조회 - 상품이 존재하지 않는 경우")
    void testGetProductList_NoProducts() {
        // Given
        ProductSearchDTO productSearchDto = new ProductSearchDTO();
        Pageable pageable = mock(Pageable.class);

        List<Product> products = new ArrayList<>();

        when(productRepository.findProducts(productSearchDto, pageable)).thenReturn(new PageImpl<>(products));

        // When
        Page<Product> resultPage = adminService.getProductList(productSearchDto, pageable);

        // Then
        assertEquals(0, resultPage.getContent().size());
        verify(productRepository, times(1)).findProducts(productSearchDto, pageable);
    }
}