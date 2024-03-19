package com.windsome.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsome.constant.OrderProductStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.constant.PaymentStatus;
import com.windsome.constant.Role;
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
import com.windsome.entity.member.Address;
import com.windsome.entity.product.Inventory;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.entity.order.OrderProduct;
import com.windsome.entity.order.Payment;
import com.windsome.entity.product.Product;
import com.windsome.exception.AdminDeletionException;
import com.windsome.repository.board.qa.QaRepository;
import com.windsome.repository.member.AddressRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.repository.orderProduct.OrderProductRepository;
import com.windsome.repository.payment.PaymentRepository;
import com.windsome.repository.product.ColorRepository;
import com.windsome.repository.product.InventoryRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.product.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final QaRepository qaRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;
    private final ObjectMapper objectMapper;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;
    private final AddressRepository addressRepository;

    /**
     * dashboard 정보 조회
     */
    @Transactional(readOnly = true)
    public DashboardInfoDto getDashboardData() {
        DashboardInfoDto dashboardInfoDto = new DashboardInfoDto();
        dashboardInfoDto.setTotalMembers(memberRepository.count());
        dashboardInfoDto.setTotalProducts(productRepository.count());
        dashboardInfoDto.setTotalQaPosts(qaRepository.count());
        dashboardInfoDto.setTotalOrderPrice(paymentRepository.getTotalPaymentPrice());
        dashboardInfoDto.setCategorySalesList(orderProductRepository.getCategorySalesCount().stream()
                .map(CategorySalesDto::new)
                .collect(Collectors.toList()));
        return dashboardInfoDto;
    }

    /**
     * 회원 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<MemberListResponseDTO> getMemberListForAdminPage(MemberListSearchDTO memberListSearchDto, Pageable pageable) {
        return memberRepository.findMembersByCriteria(memberListSearchDto, pageable);
    }

    /**
     * 회원 등록
     */
    public void enrollMember(AdminMemberFormDTO memberFormDTO) {
        Member member = modelMapper.map(memberFormDTO, Member.class);
        member.setRole(Role.USER);
        member.setPassword(passwordEncoder.encode(memberFormDTO.getPassword()));
        addressRepository.save(memberFormDTO.toAddress(member, memberFormDTO));
        memberRepository.save(member);
    }

    /**
     * 이메일 중복 체크
     */
    public boolean checkDuplicateEmail(String email) {
        return memberRepository.findByEmail(email) != null;
    }

    /**
     * 회원 상세 조회
     */
    @Transactional(readOnly = true)
    public AdminMemberDetailDTO getMemberDetails(Long accountId) {
        Member member = memberRepository.findById(accountId).orElseThrow(EntityNotFoundException::new);
        Address address = addressRepository.findByMemberIdAndIsDefault(member.getId(), true);
        return AdminMemberDetailDTO.toDto(member, address);
    }

    /**
     * 회원 수정
     */
    public void updateMember(AdminMemberDetailDTO dto) {
        Member member = memberRepository.findById(dto.getId()).orElseThrow(EntityNotFoundException::new);
        modelMapper.map(dto, member);
        // dto의 password 필드 값이 있으면 비밀번호 변경
        if (dto.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            member.setPassword(encodedPassword);
        }

        Address address = addressRepository.findByMemberIdAndIsDefault(member.getId(), true);
        address.setTel(dto.getTel());
        address.setZipcode(dto.getZipcode());
        address.setAddr(dto.getAddr());
        address.setAddrDetail(dto.getAddrDetail());
        address.setMember(member);
        addressRepository.save(address);

        memberRepository.save(member);
    }

    /**
     * 회원 권한 수정
     */
    public void updateMemberRole(Long accountId, Role role) {
        Member account = memberRepository.findById(accountId).orElseThrow(EntityNotFoundException::new);
        account.setRole(role);
        memberRepository.save(account);
    }

    /**
     * 회원 삭제
     */
    public void deleteMembers(Long[] memberIds) throws AdminDeletionException {
        for (Long memberId : memberIds) {
            Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
            if (member.getRole().equals(Role.ADMIN)) {
                throw new AdminDeletionException("관리자 권한을 가진 사용자는 삭제할 수 없습니다.");
            }
            member.setDeleted(true);
        }
    }

    /**
     * 주문 조회
     */
    public Page<OrderManagementDTO> getOrderList(String userIdentifier, Pageable pageable) {
        List<Order> orders = orderRepository.findOrderListForAdmin(userIdentifier, pageable);
        Long totalCount = orderRepository.countOrderList(userIdentifier);

        // Order -> OrderManagementDTO 변환 후 반환
        List<OrderManagementDTO> orderMngDtoList = orders.stream()
                .map(order -> {
                    OrderManagementDTO orderMngDto = new OrderManagementDTO(order);
                    orderMngDto.setOrderHistProductList(order.getOrderProducts().stream()
                            .map(orderProduct -> {
                                ProductInfoResponseDTO productInfo = productRepository.getProductInfoByProductId(orderProduct.getProduct().getId());
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
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        Payment payment = paymentRepository.findById(order.getPayment().getId()).orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findById(order.getMember().getId()).orElseThrow(EntityNotFoundException::new);

        // 1. 주문 정보 업데이트
        updateOrderInformation(order, adminPageOrderDTO, payment, member);

        // 2. 주문 상품 정보 업데이트
        updateOrderProducts(order, adminPageOrderDTO);
    }

    /**
     * 상품 조회
     */
    @Transactional(readOnly = true)
    public Page<Product> getProductList(ProductSearchDTO productSearchDto, Pageable pageable) {
        return productRepository.findProducts(productSearchDto, pageable);
    }

    /**
     * 상품 관리 - 사이즈 조회
     */
    public String getProductSizes() throws JsonProcessingException {
        return objectMapper.writeValueAsString(sizeRepository.findAll().stream()
                .map(size -> new SizeDTO(size.getId(), size.getName())).collect(Collectors.toList()));
    }

    /**
     * 상품 관리 - 색상 조회
     */
    public String getProductColors() throws JsonProcessingException {
        return objectMapper.writeValueAsString(colorRepository.findAll().stream()
                .map(color -> new ColorDTO(color.getId(), color.getName(), color.getCode())).collect(Collectors.toList()));
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
            OrderProduct orderProduct = orderProductRepository.findById(orderProductDTO.getOrderProductId()).orElseThrow(EntityNotFoundException::new);

            // 상품 옵션 변경 여부 확인
            boolean optionsChanged = !Objects.equals(orderProduct.getColor().getId(), orderProductDTO.getColorId()) || !Objects.equals(orderProduct.getSize().getId(), orderProductDTO.getSizeId());

            Inventory inventory = inventoryRepository.findByProductIdAndColorIdAndSizeId(orderProduct.getProduct().getId(), orderProduct.getColor().getId(), orderProduct.getSize().getId());
            Payment payment = paymentRepository.findById(order.getPayment().getId()).orElseThrow(EntityNotFoundException::new);
            Member member = memberRepository.findById(order.getMember().getId()).orElseThrow(EntityNotFoundException::new);
            Inventory afterInventory = inventoryRepository.findByProductIdAndColorIdAndSizeId(orderProduct.getProduct().getId(), orderProductDTO.getColorId(), orderProductDTO.getSizeId());

            // 재고 업데이트
            inventory.setQuantity(inventory.getQuantity() + orderProduct.getOrderQuantity());
            afterInventory.setQuantity(afterInventory.getQuantity() - orderProductDTO.getOrderQuantity());

            if (optionsChanged) {
                // 주문 상품의 색상 및 사이즈 업데이트
                orderProduct.setColor(colorRepository.findById(orderProductDTO.getColorId()).orElseThrow(EntityNotFoundException::new));
                orderProduct.setSize(sizeRepository.findById(orderProductDTO.getSizeId()).orElseThrow(EntityNotFoundException::new));
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

    /**
     * 주문 취소
     */
    public void cancelOrders(Long[] orderIds) {
        for (Long orderId : orderIds) {
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
    }
}
