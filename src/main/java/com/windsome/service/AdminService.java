package com.windsome.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsome.constant.OrderProductStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.constant.PaymentStatus;
import com.windsome.constant.Role;
import com.windsome.dto.board.BoardDTO;
import com.windsome.dto.board.notice.NoticeDtlDTO;
import com.windsome.dto.member.AdminMemberDetailDTO;
import com.windsome.dto.member.AdminMemberFormDTO;
import com.windsome.dto.member.MemberListResponseDTO;
import com.windsome.dto.member.MemberListSearchDTO;
import com.windsome.dto.admin.CategorySalesDto;
import com.windsome.dto.admin.DashboardInfoDto;
import com.windsome.dto.order.AdminPageOrderDTO;
import com.windsome.dto.order.AdminPageOrderProductDTO;
import com.windsome.dto.order.OrderHistProductResponseDTO;
import com.windsome.dto.product.ColorDTO;
import com.windsome.dto.product.ProductInfoResponseDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.dto.admin.OrderManagementDTO;
import com.windsome.dto.product.SizeDTO;
import com.windsome.entity.board.Board;
import com.windsome.entity.member.Address;
import com.windsome.entity.product.Inventory;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.entity.order.OrderProduct;
import com.windsome.entity.order.Payment;
import com.windsome.entity.product.Product;
import com.windsome.exception.AdminDeletionException;
import com.windsome.service.board.BoardService;
import com.windsome.service.member.AddressService;
import com.windsome.service.member.MemberService;
import com.windsome.service.order.OrderProductService;
import com.windsome.service.order.OrderService;
import com.windsome.service.order.PaymentService;
import com.windsome.service.product.ColorService;
import com.windsome.service.product.InventoryService;
import com.windsome.service.product.ProductService;
import com.windsome.service.product.SizeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final BoardService boardService;
    private final MemberService memberService;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderProductService orderProductService;
    private final SizeService sizeService;
    private final ColorService colorService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final AddressService addressService;

    /**
     * dashboard 정보 조회
     */
    @Transactional(readOnly = true)
    public DashboardInfoDto getDashboardData() {
        DashboardInfoDto dashboardInfoDto = new DashboardInfoDto();
        dashboardInfoDto.setTotalMembers(memberService.getTotalMembers());
        dashboardInfoDto.setTotalProducts(productService.getTotalProducts());
        dashboardInfoDto.setTotalQaPosts(boardService.getTotalQaPosts());
        dashboardInfoDto.setTotalOrderPrice(paymentService.getTotalPaymentPrice());
        dashboardInfoDto.setCategorySalesList(orderProductService.getCategorySalesCount() .stream()
                .map(CategorySalesDto::new)
                .collect(Collectors.toList()));
        return dashboardInfoDto;
    }

    /**
     * 상품 조회
     */
    @Transactional(readOnly = true)
    public Page<Product> getProductList(ProductSearchDTO productSearchDto, Pageable pageable) {
        return productService.getProducts(productSearchDto, pageable) ;
    }

    /**
     * 상품 관리 - 사이즈 조회
     */
    public String getProductSizes() throws JsonProcessingException {
        return objectMapper.writeValueAsString(sizeService.getSizes().stream()
                .map(size -> new SizeDTO(size.getId(), size.getName())).collect(Collectors.toList()));
    }

    /**
     * 상품 관리 - 색상 조회
     */
    public String getProductColors() throws JsonProcessingException {
        return objectMapper.writeValueAsString(colorService.getColors().stream()
                .map(color -> new ColorDTO(color.getId(), color.getName(), color.getCode())).collect(Collectors.toList()));
    }

    /**
     * 주문 조회
     */
    public Page<OrderManagementDTO> getOrderList(String userIdentifier, Pageable pageable) {
        List<Order> orders = orderService.getOrderListForAdmin(userIdentifier,pageable);
        Long totalCount = orderService.getOrderListCountForAdmin(userIdentifier);

        // Order -> OrderManagementDTO 변환 후 반환
        List<OrderManagementDTO> orderMngDtoList = orders.stream()
                .map(order -> {
                    OrderManagementDTO orderMngDto = new OrderManagementDTO(order);
                    orderMngDto.setOrderHistProductList(order.getOrderProducts().stream()
                            .map(orderProduct -> {
                                ProductInfoResponseDTO productInfo = productService.getProductInfoByProductId(orderProduct.getProduct().getId());
                                return new OrderHistProductResponseDTO(orderProduct, productInfo);
                            })
                            .collect(Collectors.toList()));
                    return orderMngDto;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(orderMngDtoList, pageable, totalCount);
    }

    /**
     * 주문 수정
     */
    public void updateOrder(Long orderId, AdminPageOrderDTO adminPageOrderDTO) {
        // 주문 및 관련 정보 조회
        Order order = orderService.getOrderByOrderId(orderId);
        Payment payment = paymentService.getPaymentByPaymentId(order.getPayment().getId());
        Member member = memberService.getMemberByMemberId(order.getMember().getId());

        // 1. 주문 정보 업데이트
        updateOrderInformation(order, adminPageOrderDTO, payment, member);

        // 2. 주문 상품 정보 업데이트
        updateOrderProducts(order, adminPageOrderDTO);
    }

    /**
     * 주문 취소
     */
    public void cancelOrders(Long[] orderIds) {
        for (Long orderId : orderIds) {
            // 주문 조회
            Order order = orderService.getOrderByOrderId(orderId);

            // 회원 포인트 관련 정보 복구
            Member member = order.getMember();
            member.setAvailablePoints(member.getAvailablePoints() - order.getEarnedPoints()); // 얻은 포인트 회수
            if (order.getUsedPoints() >= 1) {
                member.setAvailablePoints(member.getAvailablePoints() + order.getUsedPoints()); // 사용 가능한 포인트 복구
                member.setTotalUsedPoints(member.getTotalUsedPoints() - order.getUsedPoints()); // 총 사용 포인트 복구
            }
            member.setTotalEarnedPoints(member.getTotalEarnedPoints() - order.getEarnedPoints()); // 총 적립 포인트 복구
            memberService.saveMember(member);

            // 주문 취소 (주문 상태 변경, 결제 상태 변경, 주문 상품 상태 변경, 재고 수량 복구)
            order.setOrderStatus(OrderStatus.CANCELED);
            order.getPayment().setStatus(PaymentStatus.PAYMENT_CANCELLED);
            for (OrderProduct orderProduct : order.getOrderProducts()) {
                Inventory inventory = inventoryService.getInventoryByProductIdAndColorIdAndSizeId(orderProduct.getProduct().getId(), orderProduct.getColor().getId(), orderProduct.getSize().getId());
                inventory.setQuantity(inventory.getQuantity() + orderProduct.getOrderQuantity());
                orderProduct.setOrderProductStatus(OrderProductStatus.CANCELED);
            }
        }
    }

    /**
     * 회원 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<MemberListResponseDTO> getMemberListForAdminPage(MemberListSearchDTO memberListSearchDto, Pageable pageable) {
        return memberService.getMembersByCriteria(memberListSearchDto, pageable);
    }

    /**
     * 회원 등록
     */
    public void enrollMember(AdminMemberFormDTO memberFormDTO) {
        Member member = modelMapper.map(memberFormDTO, Member.class);
        member.setRole(Role.USER);
        member.setPassword(passwordEncoder.encode(memberFormDTO.getPassword()));
        addressService.saveAddress(memberFormDTO.toAddress(member, memberFormDTO));
        memberService.saveMember(member);
    }

    /**
     * 이메일 중복 체크
     */
    public boolean checkDuplicateEmail(String email) {
        return memberService.getMemberByEmail(email) != null;
    }

    /**
     * 회원 상세 조회
     */
    @Transactional(readOnly = true)
    public AdminMemberDetailDTO getMemberDetails(Long memberId) {
        Member member = memberService.getMemberByMemberId(memberId);
        Address address = addressService.getAddressByMemberIdAndIsDefault(member.getId(), true);
        return AdminMemberDetailDTO.toDto(member, address);
    }

    /**
     * 회원 수정
     */
    public void updateMember(AdminMemberDetailDTO dto) {
        Member member = memberService.getMemberByMemberId(dto.getId());
        modelMapper.map(dto, member);
        // dto의 password 필드 값이 있으면 비밀번호 변경
        if (dto.getPassword() != null) {
            member.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Address address = addressService.getAddressByMemberIdAndIsDefault(member.getId(), true);
        address.setTel(dto.getTel());
        address.setZipcode(dto.getZipcode());
        address.setAddr(dto.getAddr());
        address.setAddrDetail(dto.getAddrDetail());
        address.setMember(member);
        addressService.saveAddress(address);

        memberService.saveMember(member);
    }

    /**
     * 회원 권한 수정
     */
    public void updateMemberRole(Long memberId, Role role) {
        Member member = memberService.getMemberByMemberId(memberId);
        member.setRole(role);
        memberService.saveMember(member);
    }

    /**
     * 회원 삭제
     */
    public void deleteMembers(Long[] memberIds) throws AdminDeletionException {
        for (Long memberId : memberIds) {
            Member member = memberService.getMemberByMemberId(memberId);
            if (member.getRole().equals(Role.ADMIN)) {
                throw new AdminDeletionException("관리자 권한을 가진 사용자는 삭제할 수 없습니다.");
            }
            member.setDeleted(true);
        }
    }

    /**
     * 공지 상세 조회
     */
    public List<NoticeDtlDTO> getNoticeDtlList(Long noticeId) {
        return boardService.getNoticeDtlList(noticeId);
    }

    /**
     * 공지 등록
     */
    public Long enrollNotice(BoardDTO boardDTO, Member member) {
        Board board = modelMapper.map(boardDTO, Board.class);
        board.setMember(member);
        return boardService.saveBoard(board);
    }

    /**
     * 공지 수정 화면 - 공지 상세 조회
     */
    public Board getNotice(Long noticeId) {
        return boardService.getBoardByBoardId(noticeId);
    }

    /**
     * 공지 수정
     */
    public void updateNotice(Long noticeId, BoardDTO boardDTO) {
        Board board = boardService.getBoardByBoardId(noticeId);
        board.updateNotice(boardDTO);
        boardService.saveBoard(board);
    }

    /**
     * 공지글 설정 가능 여부 검증
     */
    public boolean checkNoticeYN(Long noticeId, boolean noticeYn) {
        Board board = boardService.getBoardByBoardId(noticeId);
        return board.isHasNotice() == noticeYn;
    }

    /**
     * 공지글 설정 수정
     */
    public void updateNoticeYN(Long noticeId, boolean noticeYn) {
        Board board = boardService.getBoardByBoardId(noticeId);
        board.setHasNotice(noticeYn);
        boardService.saveBoard(board);
    }

    /**
     * 게시글 삭제
     */
    public void deletePosts(Long[] boardIds) {
        for (Long boardId : boardIds) {
            Board board = boardService.getBoardByBoardId(boardId);
            boardService.deletePost(board.getId());
        }
    }

    private void updateOrderInformation(Order order, AdminPageOrderDTO adminPageOrderDTO, Payment payment, Member member) {
        // 주문 상태 및 관련 정보 설정
        order.setOrderStatus(OrderStatus.valueOfDisplayName(adminPageOrderDTO.getOrderStatus()));
        order.setName(adminPageOrderDTO.getRecipient());
        order.setZipcode(adminPageOrderDTO.getZipcode());
        order.setAddr(adminPageOrderDTO.getAddr());
        order.setAddrDetail(adminPageOrderDTO.getAddrDetail());
        order.setTel(adminPageOrderDTO.getTel());
        order.setReq(adminPageOrderDTO.getReq());
        // 총 주문 금액 및 결제 금액 업데이트
        order.setPrice(adminPageOrderDTO.getTotalOrderPrice());
        payment.setPrice(adminPageOrderDTO.getTotalPaymentPrice());
        // 사용 및 적립 포인트 업데이트
        order.setUsedPoints(adminPageOrderDTO.getUsedPoints());
        order.setEarnedPoints(adminPageOrderDTO.getEarnedPoints());
    }

    private void updateOrderProducts(Order order, AdminPageOrderDTO adminPageOrderDTO) {
        for (AdminPageOrderProductDTO orderProductDTO : adminPageOrderDTO.getOrderProducts()) {
            OrderProduct orderProduct = orderProductService.getOrderProductByOrderProductId(orderProductDTO.getOrderProductId());

            // 상품 옵션 변경 여부 확인
            boolean optionsChanged = !Objects.equals(orderProduct.getColor().getId(), orderProductDTO.getColorId()) || !Objects.equals(orderProduct.getSize().getId(), orderProductDTO.getSizeId());

            Inventory inventory = inventoryService.getInventoryByProductIdAndColorIdAndSizeId(orderProduct.getProduct().getId(), orderProduct.getColor().getId(), orderProduct.getSize().getId());
            Payment payment = paymentService.getPaymentByPaymentId(order.getPayment().getId());
            Member member = memberService.getMemberByMemberId(order.getMember().getId());
            Inventory afterInventory = inventoryService.getInventoryByProductIdAndColorIdAndSizeId(orderProduct.getProduct().getId(), orderProductDTO.getColorId(), orderProductDTO.getSizeId());

            // 재고 업데이트
            inventory.setQuantity(inventory.getQuantity() + orderProduct.getOrderQuantity());
            afterInventory.setQuantity(afterInventory.getQuantity() - orderProductDTO.getOrderQuantity());

            if (optionsChanged) {
                // 주문 상품의 색상 및 사이즈 업데이트
                orderProduct.setColor(colorService.getColorByColorId(orderProductDTO.getColorId()));
                orderProduct.setSize(sizeService.getSizeBySizeId(orderProductDTO.getSizeId()));
            }

            // 주문 수량이 0개인 경우
            if (orderProductDTO.getOrderQuantity() == 0) {
                // 회원 포인트 관련 정보 업데이트
                member.setAvailablePoints((int) (member.getAvailablePoints() - (orderProduct.getPrice() * 0.03)));
                member.setTotalEarnedPoints((int) (member.getTotalEarnedPoints() - (orderProduct.getPrice() * 0.03)));

                // 총 상품 금액, 적립금, 총 결제 금액 업데이트
                int orderProductTotalPrice = orderProduct.getPrice() * orderProduct.getOrderQuantity();
                order.setPrice(order.getPrice() - orderProductTotalPrice);
                order.setEarnedPoints((int) (order.getPrice() * 0.03));
                payment.setPrice(payment.getPrice() - orderProductTotalPrice);

                // 주문 상품 개수 업데이트
                order.setProductCount(order.getProductCount() - 1);

                // 주문 수량이 기존과 다른 경우
            } else if (orderProduct.getOrderQuantity() != orderProductDTO.getOrderQuantity()) {
                // 회원 포인트 관련 정보 업데이트
                int beforeEarnedPoints = (int) ((orderProduct.getPrice() * orderProduct.getOrderQuantity()) * 0.03);
                int afterEarnedPoints = (int) ((orderProduct.getPrice() * orderProductDTO.getOrderQuantity()) * 0.03);
                member.setAvailablePoints(member.getAvailablePoints() - beforeEarnedPoints + afterEarnedPoints);
                member.setTotalEarnedPoints(member.getTotalEarnedPoints() - beforeEarnedPoints + afterEarnedPoints);

                // 총 상품 금액, 적립금, 총 결제 금액 업데이트
                int beforeOrderProductTotalPrice = orderProduct.getPrice() * orderProduct.getOrderQuantity();
                int afterOrderProductTotalPrice = orderProduct.getPrice() * orderProductDTO.getOrderQuantity();
                order.setPrice(order.getPrice() - beforeOrderProductTotalPrice + afterOrderProductTotalPrice);
                order.setEarnedPoints((int) (order.getPrice() * 0.03));
                payment.setPrice(payment.getPrice() - beforeOrderProductTotalPrice + afterOrderProductTotalPrice);
            }

            // 주문 상품의 상태 및 주문 수량 업데이트
            orderProduct.setOrderQuantity(orderProductDTO.getOrderQuantity());
            orderProduct.setOrderProductStatus(OrderProductStatus.valueOfDisplayName(orderProductDTO.getOrderProductStatus()));
        }
    }
}
