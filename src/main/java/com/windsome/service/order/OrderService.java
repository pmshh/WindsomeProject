package com.windsome.service.order;

import com.windsome.constant.OrderProductStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.constant.PaymentStatus;
import com.windsome.constant.ProductSellStatus;
import com.windsome.dto.member.MemberDetailDTO;
import com.windsome.dto.order.*;
import com.windsome.dto.product.ProductInfoResponseDTO;
import com.windsome.entity.Color;
import com.windsome.entity.member.Address;
import com.windsome.entity.product.Inventory;
import com.windsome.entity.Size;
import com.windsome.entity.cart.CartProduct;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.entity.order.OrderProduct;
import com.windsome.entity.order.Payment;
import com.windsome.entity.product.Product;
import com.windsome.repository.order.OrderRepository;
import com.windsome.service.member.AddressService;
import com.windsome.service.cart.CartProductService;
import com.windsome.service.member.MemberService;
import com.windsome.service.product.*;
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

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final MemberService memberService;
    private final AddressService addressService;
    private final ProductImageService productImageService;
    private final CartProductService cartProductService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final OrderProductService orderProductService;
    private final ColorService colorService;
    private final SizeService sizeService;

    public Order getOrderByOrderId(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
    }

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
                                ProductInfoResponseDTO productInfo = productService.getProductInfoByProductId(orderProduct.getProduct().getId());
                                return new OrderHistProductResponseDTO(orderProduct, productInfo);
                            })
                            .collect(Collectors.toList()));
                    return orderHistDto;
                })
                .collect(Collectors.toList());
        return new PageImpl<OrderHistResponseDTO>(orderHistDtoList, pageable, totalCount);
    }

    /**
     * 주문서 작성 화면 - 회원 상세 정보 조회
     */
    public MemberDetailDTO getMemberDetail(Long memberId) {
        Member member = memberService.getMemberByMemberId(memberId);
        Address address = addressService.getAddressByMemberIdAndIsDefault(member.getId(), true);
        return MemberDetailDTO.createMemberDetailDTO(member, address);
    }

    /**
     * 주문서 작성 화면 - 회원 기본 배송지 조회
     */
    public Address getDefaultShippingAddress(Long memberId) {
        return addressService.getAddressByMemberIdAndIsDefault(memberId, true);
    }

    /**
     * 주문서 작성 화면 - 회원 배송지 목록 조회
     */
    public List<Address> getAddressList(Long memberId) {
        return addressService.getAllAddressByMemberId(memberId);
    }

    /**
     * 주문서 작성 화면 - 주문 상품 정보 조회
     */
    public List<OrderProductResponseDTO> getOrderProductsInfo(OrderProductListDTO orderProductListDTO) {
        return orderProductListDTO.getOrderProducts().stream()
                .map(orderProductDTO -> {
                    Product product = productService.getProductByProductId(orderProductDTO.getProductId());
                    String repImageUrl = productImageService.getRepresentativeImageUrl(orderProductDTO.getProductId(), true);
                    CartProduct cartProduct = cartProductService.getCartProductByProductIdAndColorIdAndSizeId(product.getId(), orderProductDTO.getColorId(), orderProductDTO.getSizeId());
                    return OrderProductResponseDTO.createDTO(orderProductDTO, product, repImageUrl, cartProduct);
                })
                .collect(Collectors.toList());
    }

    /**
     * 상품 주문
     */
    public Long order(OrderRequestDTO orderRequestDTO, Long memberId) {
        // 회원 정보 DB 조회
        Member member = memberService.getMemberByMemberId(memberId);

        // DB에 저장할 주문 상품 리스트 생성
        List<OrderProduct> orderProductList = createOrderProductList(orderRequestDTO.getOrderProductDtoList());

        // 회원 포인트 관련 정보 업데이트
        initializePointData(member, orderRequestDTO);

        // 회원 배송지 관련 정보 업데이트
        updateMemberDefaultAddress(orderRequestDTO, member);
        memberService.saveMember(member);

        // 결제 정보 저장
        Payment payment = Payment.createPayment(orderRequestDTO);
        paymentService.savePayment(payment);

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
    public boolean verifyOrderCancellationPermission(Long orderId, Long memberId) {
        // 현재 로그인 한 계정과, 주문한 계정 비교
        Member currentAccount = memberService.getMemberByMemberId(memberId);
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

    /**
     * 주문 상세 조회
     */
    public OrderDetailDTO getOrderDetail(Long orderId) {
        // 주문, 주문 상품 정보 조회
        OrderDetailDTO orderDetail = orderRepository.getOrderDetail(orderId);
        List<OrderProduct> orderProductList = orderProductService.getOrderProductsByOrderId(orderId);

        // 주문 상세 상품 정보 리스트 생성
        List<OrderDetailProductDTO> orderDetailProductDTOList = orderProductList.stream().map(orderProduct -> {
            // 대표 이미지 URL 조회
            String repImageUrl = productImageService.getRepresentativeImageUrl(orderProduct.getProduct().getId(), true);
            // 재고 정보 조회
            Inventory inventory = inventoryService.getInventoryByProductIdAndColorIdAndSizeId(orderProduct.getProduct().getId(), orderProduct.getColor().getId(), orderProduct.getSize().getId());
            // OrderDetailProductDTO 생성
            return OrderDetailProductDTO.createDTO(orderProduct, repImageUrl, inventory);
        }).collect(Collectors.toList());
        // 주문 상세 정보에 상세 상품 리스트 설정
        orderDetail.setOrderDetailProductList(orderDetailProductDTOList);

        return orderDetail;
    }

    /**
     * 관리자 페이지 - 주문 조회
     */
    public List<Order> getOrderListForAdmin(String userIdentifier, Pageable pageable) {
        return orderRepository.findOrderListForAdmin(userIdentifier, pageable);
    }

    /**
     * 관리자 페이지 - 주문 조회 카운트 쿼리
     */
    public Long getOrderListCountForAdmin(String userIdentifier) {
        return orderRepository.countOrderList(userIdentifier);
    }

    private List<OrderProduct> createOrderProductList(List<OrderProductRequestDTO> orderProductRequestDTOList) {
        return orderProductRequestDTOList.stream()
                .map(orderProductRequestDto -> {
                    // 상품, 색상, 사이즈 조회
                    Product product = productService.getProductByProductId(orderProductRequestDto.getProductId());
                    Color color = colorService.getColorByColorId(orderProductRequestDto.getColorId());
                    Size size = sizeService.getSizeBySizeId(orderProductRequestDto.getSizeId());

                    // 상품 재고 감소
                    Inventory inventory = inventoryService.getInventoryByProductIdAndColorIdAndSizeId(product.getId(), color.getId(), size.getId());
                    inventory.removeStock(orderProductRequestDto.getOrderQuantity());

                    // 상품의 재고가 0개인 경우 품절 처리
                    List<Inventory> inventories = inventoryService.getInventoriesByProductId(product.getId());
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

    private void updateMemberDefaultAddress(OrderRequestDTO orderRequestDTO, Member member) {
        if (orderRequestDTO.isDefault()) {
            // 기존의 주소 목록 가져오기
            List<Address> existingAddresses = addressService.getAllAddressByMemberId(member.getId());

            // 기본 배송지 모두 false로 초기화
            existingAddresses.forEach(address -> {
                address.setDefault(false);
                addressService.saveAddress(address); // 기존 주소의 default 값을 변경한 후 저장
            });

            // 새로운 배송지 생성
            Address newAddress = OrderRequestDTO.toAddress(member, orderRequestDTO);

            // 새로운 배송지와 일치하는 기존 주소가 있는지 확인
            boolean isDuplicate = existingAddresses.stream().anyMatch(address -> address.equals(newAddress));

            // 중복되는 주소가 없는 경우 배송지 추가
            if (!isDuplicate) {
                // 새로운 배송지 추가
                addressService.saveAddress(newAddress);
            } else {
                // 기존의 주소와 동일한 경우 isDefault 값을 true로 변경
                existingAddresses.stream()
                        .filter(existingAddress -> existingAddress.equals(newAddress))
                        .findFirst()
                        .ifPresent(existingAddress -> {
                            existingAddress.setDefault(true);
                            existingAddress.setReq(orderRequestDTO.getReq());
                            addressService.saveAddress(existingAddress);
                        });
            }
        }
    }

    private void deleteCartProducts(List<OrderProduct> orderProductList) {
        orderProductList.stream()
                .map(orderProduct -> cartProductService.getCartProductByProductIdAndColorIdAndSizeId(orderProduct.getProduct().getId(), orderProduct.getColor().getId(), orderProduct.getSize().getId()))
                .filter(Objects::nonNull)
                .forEach(cartProductService::deleteCartProduct);
    }
}
