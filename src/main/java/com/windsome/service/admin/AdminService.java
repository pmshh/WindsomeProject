package com.windsome.service.admin;

import com.windsome.constant.OrderProductStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.constant.Role;
import com.windsome.dto.board.BoardDTO;
import com.windsome.dto.member.AdminMemberDetailDTO;
import com.windsome.dto.member.AdminMemberFormDTO;
import com.windsome.dto.member.MemberListResponseDTO;
import com.windsome.dto.member.MemberListSearchDTO;
import com.windsome.dto.admin.CategorySalesDTO;
import com.windsome.dto.admin.DashboardInfoDTO;
import com.windsome.dto.order.AdminPageOrderDTO;
import com.windsome.dto.order.AdminPageOrderProductDTO;
import com.windsome.dto.order.OrderHistProductResponseDTO;
import com.windsome.dto.product.ProductInfoResponseDTO;
import com.windsome.dto.product.ProductOptionDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.dto.admin.OrderManagementDTO;
import com.windsome.entity.board.Board;
import com.windsome.entity.member.Address;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.entity.order.OrderProduct;
import com.windsome.entity.order.Payment;
import com.windsome.entity.product.Product;
import com.windsome.entity.product.ProductOption;
import com.windsome.exception.AdminDeletionException;
import com.windsome.repository.member.MemberRepository;
import com.windsome.service.board.BoardService;
import com.windsome.service.member.AddressService;
import com.windsome.service.member.MemberService;
import com.windsome.service.order.OrderProductService;
import com.windsome.service.order.OrderService;
import com.windsome.service.order.PaymentService;
import com.windsome.service.product.ProductOptionService;
import com.windsome.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    private final BoardService boardService;
    private final MemberService memberService;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderProductService orderProductService;
    private final PaymentService paymentService;
    private final AddressService addressService;
    private final ProductOptionService productOptionService;

    /**
     * dashboard 정보 조회
     */
    @Transactional(readOnly = true)
    public DashboardInfoDTO getDashboardData() {
        DashboardInfoDTO dashboardInfoDto = new DashboardInfoDTO();
        dashboardInfoDto.setTotalMembers(memberService.getTotalMembers());
        dashboardInfoDto.setTotalProducts(productService.getTotalProducts());
        dashboardInfoDto.setTotalQaPosts(boardService.getTotalQaPosts());
        dashboardInfoDto.setTotalOrderPrice(paymentService.getTotalPaymentPrice());
        dashboardInfoDto.setCategorySalesList(orderProductService.getCategorySalesCount() .stream()
                .map(CategorySalesDTO::new)
                .collect(Collectors.toList()));
        return dashboardInfoDto;
    }

    /**
     * 상품 전체 조회
     */
    @Transactional(readOnly = true)
    public Page<Product> getProductList(ProductSearchDTO productSearchDto, Pageable pageable) {
        return productService.getProducts(productSearchDto, pageable) ;
    }

    /**
     * 상품 옵션 조회
     */
    public List<ProductOptionDTO> getProductOptionsByProductId(Long productId) {
        // 상품 옵션 리스트 조회
        List<ProductOption> productOptions = productOptionService.getProductOptionsByProductId(productId);

        // Entity -> DTO 변환
        List<ProductOptionDTO> productOptionListDTO = new ArrayList<>();
        for (ProductOption option : productOptions) {
            ProductOptionDTO productOptionDTO = ProductOptionDTO.builder().color(option.getColor()).size(option.getSize()).build();
            productOptionListDTO.add(productOptionDTO);
        }
        return productOptionListDTO;
    }

    /**
     * 주문 전체 조회
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

        // 주문 정보 수정
        updateOrderInformation(order, adminPageOrderDTO, payment);

        // 주문 상품 정보 수정
        updateOrderProducts(adminPageOrderDTO, member);
    }

    /**
     * 주문 취소
     */
    public void cancelOrders(Long[] orderIds) {
        for (Long orderId : orderIds) {
            orderService.cancelOrder(orderId);
        }
    }

    /**
     * 회원 전체 조회
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
    public void updateMember(AdminMemberDetailDTO adminMemberDetailDTO) {
        // 회원 조회
        Member member = memberService.getMemberByMemberId(adminMemberDetailDTO.getId());

        // 회원 수정
        modelMapper.map(adminMemberDetailDTO, member);
        if (adminMemberDetailDTO.getPassword() != null) { // adminMemberDetailDTO의 password 필드 값이 있으면 비밀번호 encoding 후 비밀번호 저장
            member.setPassword(passwordEncoder.encode(adminMemberDetailDTO.getPassword()));
        }

        // 회원 배송지 수정 및 저장
        Address address = addressService.getAddressByMemberIdAndIsDefault(member.getId(), true);
        address.setZipcode(adminMemberDetailDTO.getZipcode());
        address.setAddr(adminMemberDetailDTO.getAddr());
        address.setAddrDetail(adminMemberDetailDTO.getAddrDetail());
        address.setTel(adminMemberDetailDTO.getTel());
        addressService.saveAddress(address);

        // 회원 저장
        memberService.saveMember(member); // 회원 저장
    }

    /**
     * 회원 권한 수정
     */
    public void updateMemberRole(Long memberId, Role role) {
        Member member = memberService.getMemberByMemberId(memberId); // 회원 조회
        member.setRole(role); // 회원 권한 수정
        memberService.saveMember(member); // 회원 저장
    }

    /**
     * 회원 삭제
     */
    public void deleteMembers(Long[] memberIds) throws AdminDeletionException {
        for (Long memberId : memberIds) {
            // 회원 조회
            Member member = memberService.getMemberByMemberId(memberId);

            // 관리자 권한을 가진 회원은 삭제 불가
            if (member.getRole().equals(Role.ADMIN)) {
                throw new AdminDeletionException("관리자 권한을 가진 사용자는 삭제할 수 없습니다.");
            }

            // 회원 삭제 처리 (실제 DB에서 삭제되지는 않고 isDeleted 필드 값만 true로 수정)
            member.setDeleted(true);
        }
    }

    /**
     * 공지 등록
     */
    public Long enrollNotice(BoardDTO boardDTO, Member member) {
        Board board = modelMapper.map(boardDTO, Board.class); // DTO -> Entity 변환
        board.setMember(member);
        return boardService.saveBoard(board); // Board 저장
    }

    /**
     * 공지 수정
     */
    public void updateNotice(Long noticeId, BoardDTO boardDTO) {
        Board board = boardService.getBoardByBoardId(noticeId); // Board 조회
        board.updateNotice(boardDTO);
        boardService.saveBoard(board); // Board 저장
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

    private void updateOrderInformation(Order order, AdminPageOrderDTO adminPageOrderDTO, Payment payment) {
        // 주문 정보 수정
        order.setOrderStatus(OrderStatus.valueOfDisplayName(adminPageOrderDTO.getOrderStatus()));
        order.setName(adminPageOrderDTO.getRecipient());
        order.setZipcode(adminPageOrderDTO.getZipcode());
        order.setAddr(adminPageOrderDTO.getAddr());
        order.setAddrDetail(adminPageOrderDTO.getAddrDetail());
        order.setTel(adminPageOrderDTO.getTel());
        order.setReq(adminPageOrderDTO.getReq());
        order.setPrice(adminPageOrderDTO.getTotalOrderPrice());
        order.setUsedPoints(adminPageOrderDTO.getUsedPoints());
        order.setEarnedPoints(adminPageOrderDTO.getEarnedPoints());
        order.setProductCount(adminPageOrderDTO.getOrderProducts().size());

        // 결제 금액 수정
        payment.setPrice(adminPageOrderDTO.getTotalPaymentPrice());
    }

    private void updateOrderProducts(AdminPageOrderDTO adminPageOrderDTO, Member member) {
        for (AdminPageOrderProductDTO orderProductDTO : adminPageOrderDTO.getOrderProducts()) {
            // 주문 상품 조회
            OrderProduct orderProduct = orderProductService.getOrderProductByOrderProductId(orderProductDTO.getOrderProductId());
            Product product = orderProduct.getProduct();
            ProductOption beforeProductOption = null;
            ProductOption afterProductOption = null;

            // 주문한 상품에 옵션이 있는 경우 옵션 및 재고 수정
            if (!Objects.equals(orderProduct.getColor(), "N/A")) {
                beforeProductOption = productOptionService.getProductOptionByProductIdAndColorAndSize(product.getId(), orderProduct.getColor(), orderProduct.getSize());
                afterProductOption = productOptionService.getProductOptionByProductIdAndColorAndSize(product.getId(), orderProductDTO.getColor(), orderProductDTO.getSize());

                beforeProductOption.setQuantity(beforeProductOption.getQuantity() + orderProduct.getOrderQuantity());
                afterProductOption.setQuantity(afterProductOption.getQuantity() - orderProductDTO.getOrderQuantity());

                orderProduct.setColor(orderProductDTO.getColor());
                orderProduct.setSize(orderProductDTO.getSize());
            // 주문한 상품에 옵션이 없는 경우 상품의 재고만 수정
            } else {
                product.setInventory(product.getInventory() + orderProduct.getOrderQuantity() - orderProductDTO.getOrderQuantity());
            }

            // 주문 수량에 따른 회원 포인트 정보 수정 - 주문 수량이 0개인 경우
            if (orderProductDTO.getOrderQuantity() == 0) {
                member.setAvailablePoints((int) (member.getAvailablePoints() - (orderProduct.getPrice() * 0.03)));
                member.setTotalEarnedPoints((int) (member.getTotalEarnedPoints() - (orderProduct.getPrice() * 0.03)));
            // 주문 수량에 따른 회원 포인트 정보 수정 - 주문 수량이 기존과 다른 경우
            } else if (orderProduct.getOrderQuantity() != orderProductDTO.getOrderQuantity()) {
                int beforeEarnedPoints = (int) ((orderProduct.getPrice() * orderProduct.getOrderQuantity()) * 0.03);
                int afterEarnedPoints = (int) ((orderProduct.getPrice() * orderProductDTO.getOrderQuantity()) * 0.03);
                int pointsDifference = afterEarnedPoints - beforeEarnedPoints;
                member.setAvailablePoints(member.getAvailablePoints() - pointsDifference);
                member.setTotalEarnedPoints(member.getTotalEarnedPoints() - pointsDifference);
            }

            // 주문 상품의 상태 및 주문 수량 수정
            orderProduct.setOrderQuantity(orderProductDTO.getOrderQuantity());
            orderProduct.setOrderProductStatus(OrderProductStatus.valueOfDisplayName(orderProductDTO.getOrderProductStatus()));
        }
    }
}
