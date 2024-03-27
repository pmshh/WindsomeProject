package com.windsome.service;

import com.windsome.constant.Role;
import com.windsome.dto.admin.CategorySalesResult;
import com.windsome.dto.admin.DashboardInfoDTO;
import com.windsome.dto.admin.OrderManagementDTO;
import com.windsome.dto.board.BoardDTO;
import com.windsome.dto.member.AdminMemberDetailDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.entity.board.Board;
import com.windsome.entity.member.Address;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.entity.product.Product;
import com.windsome.exception.AdminDeletionException;
import com.windsome.service.admin.AdminService;
import com.windsome.service.board.BoardService;
import com.windsome.service.member.AddressService;
import com.windsome.service.member.MemberService;
import com.windsome.service.order.OrderProductService;
import com.windsome.service.order.OrderService;
import com.windsome.service.order.PaymentService;
import com.windsome.service.product.ProductService;
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

import static com.windsome.TestUtil.createMember;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class AdminServiceTest {

    @Mock private ModelMapper modelMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MemberService memberService;
    @Mock private AddressService addressService;
    @Mock private ProductService productService;
    @Mock private PaymentService paymentService;
    @Mock private OrderService orderService;
    @Mock private OrderProductService orderProductService;
    @Mock private BoardService boardService;

    @InjectMocks private AdminService adminService;

    @Test
    @DisplayName("대시보드 데이터 조회")
    public void testGetDashboardData() {
        // Given
        when(memberService.getTotalMembers()).thenReturn(10L);
        when(productService.getTotalProducts()).thenReturn(20L);
        when(boardService.getTotalQaPosts()).thenReturn(5L);
        when(paymentService.getTotalPaymentPrice()).thenReturn(500L);

        CategorySalesResult categorySalesResult1 = mock(CategorySalesResult.class);
        when(categorySalesResult1.getCategory()).thenReturn(1L);
        when(categorySalesResult1.getOrderQuantity()).thenReturn(100L);

        CategorySalesResult categorySalesResult2 = mock(CategorySalesResult.class);
        when(categorySalesResult2.getCategory()).thenReturn(2L);
        when(categorySalesResult2.getOrderQuantity()).thenReturn(200L);

        List<CategorySalesResult> categorySalesResults = Arrays.asList(categorySalesResult1, categorySalesResult2);
        when(orderProductService.getCategorySalesCount()).thenReturn(categorySalesResults);

        // When
        DashboardInfoDTO dashboardInfoDto = adminService.getDashboardData();

        // Then
        verify(memberService, times(1)).getTotalMembers();
        verify(productService, times(1)).getTotalProducts();
        verify(boardService, times(1)).getTotalQaPosts();
        verify(paymentService, times(1)).getTotalPaymentPrice();
        verify(orderProductService, times(1)).getCategorySalesCount();

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

    /**
     * 상품 관리 TEST
     */
    @Test
    @DisplayName("상품 조회 - 상품이 존재하는 경우")
    void testGetProductList_ProductsExist() {
        // Given
        ProductSearchDTO productSearchDto = new ProductSearchDTO();
        Pageable pageable = mock(Pageable.class);

        List<Product> products = new ArrayList<>();
        products.add(new Product());
        products.add(new Product());

        when(productService.getProducts(productSearchDto, pageable)).thenReturn(new PageImpl<>(products));

        // When
        Page<Product> resultPage = adminService.getProductList(productSearchDto, pageable);

        // Then
        assertEquals(2, resultPage.getContent().size());
        verify(productService, times(1)).getProducts(productSearchDto, pageable);
    }

    @Test
    @DisplayName("상품 조회 - 상품이 존재하지 않는 경우")
    void testGetProductList_NoProducts() {
        // Given
        ProductSearchDTO productSearchDto = new ProductSearchDTO();
        Pageable pageable = mock(Pageable.class);

        List<Product> products = new ArrayList<>();

        when(productService.getProducts(productSearchDto, pageable)).thenReturn(new PageImpl<>(products));

        // When
        Page<Product> resultPage = adminService.getProductList(productSearchDto, pageable);

        // Then
        assertEquals(0, resultPage.getContent().size());
        verify(productService, times(1)).getProducts(productSearchDto, pageable);
    }

    /**
     * 주문 관리 TEST
     */
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

        when(orderService.getOrderListForAdmin(userIdentifier, pageable)).thenReturn(orders);
        when(orderService.getOrderListCountForAdmin(userIdentifier)).thenReturn(2L);

        // When
        Page<OrderManagementDTO> resultPage = adminService.getOrderList(userIdentifier, pageable);

        // Then
        assertEquals(2, resultPage.getContent().size());
        assertEquals(2L, resultPage.getTotalElements());
        verify(orderService, times(1)).getOrderListForAdmin(userIdentifier, pageable);
        verify(orderService, times(1)).getOrderListCountForAdmin(userIdentifier);
    }

    @Test
    @DisplayName("주문 조회 - 주문이 존재하지 않는 경우")
    void testGetOrderList_NoOrders() {
        // Given
        String userIdentifier = "user123";
        Pageable pageable = mock(Pageable.class);

        List<Order> orders = new ArrayList<>();

        when(orderService.getOrderListForAdmin(userIdentifier, pageable)).thenReturn(orders);
        when(orderService.getOrderListCountForAdmin(userIdentifier)).thenReturn(0L);

        // When
        Page<OrderManagementDTO> resultPage = adminService.getOrderList(userIdentifier, pageable);

        // Then
        assertEquals(0, resultPage.getContent().size());
        assertEquals(0L, resultPage.getTotalElements());
        verify(orderService, times(1)).getOrderListForAdmin(userIdentifier, pageable);
        verify(orderService, times(1)).getOrderListCountForAdmin(userIdentifier);
    }

    /**
     * 회원 관리 TEST
     */
    @Test
    @DisplayName("회원 상세 정보 조회")
    public void testGetMemberDetails() {
        // Given
        Long accountId = 1L;
        Member member = createMember(accountId);
        when(memberService.getMemberByMemberId(accountId)).thenReturn(member);
        when(addressService.getAddressByMemberIdAndIsDefault(accountId, true)).thenReturn(new Address());

        // When
        AdminMemberDetailDTO result = adminService.getMemberDetails(accountId);

        // Then
        verify(memberService, times(1)).getMemberByMemberId(accountId);
        verify(addressService, times(1)).getAddressByMemberIdAndIsDefault(accountId, true);
        assertEquals(member.getId(), result.getId());
        assertEquals(member.getUserIdentifier(), result.getUserIdentifier());
        assertEquals(member.getPassword(), result.getPassword());
        assertEquals(member.getName(), result.getName());
        assertEquals(member.getEmail(), result.getEmail());
        assertEquals(member.getAvailablePoints(), result.getAvailablePoints());
        assertEquals(member.getTotalUsedPoints(), result.getTotalUsedPoints());
        assertEquals(member.getTotalEarnedPoints(), result.getTotalEarnedPoints());
    }

    @Test
    @DisplayName("회원 상세 정보 조회 시 EntityNotFoundException 발생")
    public void testGetMemberDetails_ThrowsEntityNotFoundException() {
        // Given
        Long accountId = 1L;
        when(memberService.getMemberByMemberId(accountId)).thenThrow(EntityNotFoundException.class);

        // When
        Executable executable = () -> adminService.getMemberDetails(accountId);

        // Then
        assertThrows(EntityNotFoundException.class, executable);
        verify(memberService, times(1)).getMemberByMemberId(accountId);
    }

    @Test
    @DisplayName("회원 상세 정보 업데이트")
    public void testUpdateMemberDetails() {
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
        member.setAvailablePoints(dto.getAvailablePoints());
        member.setTotalUsedPoints(dto.getTotalUsedPoints());
        member.setTotalEarnedPoints(dto.getTotalEarnedPoints());

        when(memberService.getMemberByMemberId(dto.getId())).thenReturn(member);
        when(passwordEncoder.encode("newpassword")).thenReturn("$2a$10$gLKb.8YwrDpQVmbZpRiMzOaEmI6oUxgWDEO75nKoqyQKOWoBvC.Ci");
        when(addressService.getAddressByMemberIdAndIsDefault(member.getId(), true)).thenReturn(new Address());

        // When
        adminService.updateMember(dto);

        // Then
        verify(memberService, times(1)).getMemberByMemberId(dto.getId());
        verify(modelMapper, times(1)).map(dto, member);
        verify(passwordEncoder, times(1)).encode(dto.getPassword());
        verify(memberService, times(1)).saveMember(member);
        verify(addressService, times(1)).saveAddress(any(Address.class));

        // Verify that the member was updated correctly
        assertEquals(dto.getId(), member.getId());
        assertEquals(dto.getUserIdentifier(), member.getUserIdentifier());
        assertEquals("$2a$10$gLKb.8YwrDpQVmbZpRiMzOaEmI6oUxgWDEO75nKoqyQKOWoBvC.Ci", member.getPassword());
        assertEquals(dto.getName(), member.getName());
        assertEquals(dto.getEmail(), member.getEmail());
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

        when(memberService.getMemberByMemberId(dto.getId())).thenThrow(EntityNotFoundException.class);

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> adminService.updateMember(dto));
        verify(memberService, times(1)).getMemberByMemberId(dto.getId());
        verifyNoMoreInteractions(modelMapper, passwordEncoder, memberService);
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

        when(memberService.getMemberByMemberId(accountId)).thenReturn(existingMember);

        // When
        adminService.updateMemberRole(accountId, newRole);

        // Then
        verify(memberService, times(1)).getMemberByMemberId(accountId);
        verify(memberService, times(1)).saveMember(existingMember);

        assertEquals(newRole, existingMember.getRole());
    }

    @Test
    @DisplayName("회원 권한 업데이트 시 EntityNotFoundException 발생")
    void testUpdateMemberRole_EntityNotFoundException() {
        // Given
        Long accountId = 1L;
        Role newRole = Role.ADMIN;

        when(memberService.getMemberByMemberId(accountId)).thenThrow(EntityNotFoundException.class);

        // When, Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            adminService.updateMemberRole(accountId, newRole);
        });

        assertEquals(EntityNotFoundException.class, exception.getClass());
        verify(memberService, times(1)).getMemberByMemberId(accountId);
        verify(memberService, never()).saveMember(any());
    }

    @Test
    @DisplayName("회원 삭제 - 회원이 존재하는 경우")
    void testDeleteMember() throws AdminDeletionException {
        // Given
        Long accountId = 1L;
        Long[] accountIds = {1L};
        Member member = new Member();
        member.setRole(Role.USER);

        when(memberService.getMemberByMemberId(accountId)).thenReturn(member);

        // When
        adminService.deleteMembers(accountIds);

        // Then
        assertEquals(member.isDeleted(), true);
        verify(memberService, times(1)).getMemberByMemberId(accountId);
    }

    @Test
    @DisplayName("회원 삭제 - 회원이 관리자인 경우")
    void testDeleteMember_AdminRole() {
        // Given
        Long accountId = 1L;
        Long[] accountIds = {1L};
        Member adminMember = new Member();
        adminMember.setRole(Role.ADMIN);

        when(memberService.getMemberByMemberId(accountId)).thenReturn(adminMember);

        // When, Then
        AdminDeletionException exception = assertThrows(AdminDeletionException.class, () -> {
            adminService.deleteMembers(accountIds);
        });
        assertEquals(adminMember.isDeleted(), false);
        verify(memberService, times(1)).getMemberByMemberId(accountId);
    }

    @Test
    @DisplayName("회원 삭제 - 회원이 존재하지 않는 경우")
    void testDeleteMember_EntityNotFoundException() {
        // Given
        Long accountId = 1L;
        Long[] accountIds = {1L};
        when(memberService.getMemberByMemberId(accountId)).thenThrow(EntityNotFoundException.class);

        // When, Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            adminService.deleteMembers(accountIds);
        });

        verify(memberService, times(1)).getMemberByMemberId(accountId);
    }

    /**
     * 게시판 관리 TEST
     */
    @Test
    @DisplayName("공지사항 등록")
    public void testEnrollNotice() {
        // given
        BoardDTO boardDTO = new BoardDTO();
        Member member = new Member();

        Board board = new Board();
        board.setId(1L);
        board.setMember(member);

        when(boardService.saveBoard(any())).thenReturn(board.getId());
        when(modelMapper.map(boardDTO, Board.class)).thenReturn(board);

        // when
        Long savedNoticeId = adminService.enrollNotice(boardDTO, member);

        // then
        verify(boardService, times(1)).saveBoard(any());
        assertEquals(1L, savedNoticeId);
    }

    @Test
    @DisplayName("공지사항 수정 - 성공")
    public void testUpdateNotice_Success() {
        // given
        Long noticeId = 1L;
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setTitle("수정 test");
        boardDTO.setContent("수정 test");
        boardDTO.setHasNotice(false);

        Board existingNotice = new Board();
        existingNotice.setId(noticeId);
        existingNotice.setTitle("기존 제목");
        existingNotice.setContent("기존 내용");
        existingNotice.setRegTime(LocalDateTime.now());

        when(boardService.getBoardByBoardId(anyLong())).thenReturn(existingNotice);

        // when
        adminService.updateNotice(noticeId, boardDTO);

        // then
        verify(boardService, times(1)).getBoardByBoardId(noticeId);
        verify(boardService, times(1)).saveBoard(existingNotice);
        assertEquals("수정 test", existingNotice.getTitle());
        assertEquals("수정 test", existingNotice.getContent());
    }

    @Test
    @DisplayName("공지사항 수정 - 공지사항을 찾을 수 없는 경우")
    public void testUpdateNotice_NotFound() {
        // given
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setTitle("수정 test");
        boardDTO.setContent("수정 test");
        boardDTO.setHasNotice(false);
        Long noticeId = 1L;

        when(boardService.getBoardByBoardId(anyLong())).thenThrow(EntityNotFoundException.class);

        // when, then
        assertThrows(EntityNotFoundException.class, () -> adminService.updateNotice(noticeId, boardDTO));
        verify(boardService, times(1)).getBoardByBoardId(noticeId);
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("공지글 설정 가능 여부 검증 - 가능")
    public void testCheckNoticeYN_NoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYN = true;

        Board notice = new Board();
        notice.setHasNotice(false);

        when(boardService.getBoardByBoardId(noticeId)).thenReturn(notice);

        // when
        boolean result = adminService.checkNoticeYN(noticeId, noticeYN);

        // then
        assertFalse(result);
    }


    @Test
    @DisplayName("공지글 설정 가능 여부 검증 - 불가능(이미 공지글로 설정된 경우)")
    public void testCheckNoticeYN_NotNoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYN = true;

        Board notice = new Board();
        notice.setHasNotice(true);

        when(boardService.getBoardByBoardId(noticeId)).thenReturn(notice);

        // when
        boolean result = adminService.checkNoticeYN(noticeId, noticeYN);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("공지글 설정 가능 여부 검증 - 존재하지 않는 공지글")
    public void testCheckNoticeYN_EntityNotFoundException() {
        // given
        Long noticeId = 1L;
        boolean noticeYN = true;
        when(boardService.getBoardByBoardId(noticeId)).thenThrow(EntityNotFoundException.class);

        // when, then
        assertThrows(EntityNotFoundException.class, () -> adminService.checkNoticeYN(noticeId, noticeYN));
    }

    @Test
    @DisplayName("공지글 설정 수정 - 공지글로 변경")
    public void testUpdateNoticeYN_SetNoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYn = true;

        Board notice = new Board();
        notice.setHasNotice(false);

        when(boardService.getBoardByBoardId(noticeId)).thenReturn(notice);

        // when
        adminService.updateNoticeYN(noticeId, noticeYn);

        // then
        assertTrue(notice.isHasNotice());
        verify(boardService).saveBoard(notice);
    }

    @Test
    @DisplayName("공지글 설정 수정 - 공지글 해제")
    public void testUpdateNoticeYN_UnsetNoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYn = false;

        Board notice = new Board();
        notice.setHasNotice(true);

        when(boardService.getBoardByBoardId(noticeId)).thenReturn(notice);

        // when
        adminService.updateNoticeYN(noticeId, noticeYn);

        // then
        assertFalse(notice.isHasNotice());
        verify(boardService).saveBoard(notice);
    }

    @Test
    @DisplayName("공지글 설정 수정 - 존재하지 않는 공지글")
    public void testUpdateNoticeYN_EntityNotFoundException() {
        // given
        Long noticeId = 1L;
        boolean noticeYn = true;

        when(boardService.getBoardByBoardId(noticeId)).thenThrow(EntityNotFoundException.class);

        // when, then
        assertThrows(EntityNotFoundException.class, () -> adminService.updateNoticeYN(noticeId, noticeYn));
        verify(boardService, never()).saveBoard(any());
    }

    @Test
    @DisplayName("게시글 여러건 삭제 - 성공")
    public void testDeletePosts_Success() {
        // given
        Long[] noticeIds = {1L, 2L, 3L};
        Board notice1 = new Board();
        Board notice2 = new Board();
        Board notice3 = new Board();
        when(boardService.getBoardByBoardId(1L)).thenReturn(notice1);
        when(boardService.getBoardByBoardId(2L)).thenReturn(notice2);
        when(boardService.getBoardByBoardId(3L)).thenReturn(notice3);

        // when
        adminService.deletePosts(noticeIds);

        // then
        verify(boardService, times(3)).getBoardByBoardId(any());
        verify(boardService, times(3)).deletePost(any());
    }

    @Test
    @DisplayName("게시글 여러건 삭제 - 존재하지 않는 게시글")
    public void testDeletePosts_EntityNotFoundException() {
        // given
        Long[] noticeIds = {1L, 2L, 3L};
        when(boardService.getBoardByBoardId(anyLong())).thenThrow(EntityNotFoundException.class);

        // when, then
        assertThrows(EntityNotFoundException.class, () -> adminService.deletePosts(noticeIds));
    }
}