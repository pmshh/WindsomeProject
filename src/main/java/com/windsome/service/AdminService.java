package com.windsome.service;

import com.windsome.constant.Role;
import com.windsome.dto.member.AdminMemberDetailDTO;
import com.windsome.dto.member.MemberListResponseDTO;
import com.windsome.dto.member.MemberListSearchDto;
import com.windsome.dto.admin.CategorySalesDto;
import com.windsome.dto.admin.DashboardInfoDto;
import com.windsome.dto.order.OrderProductDto;
import com.windsome.dto.product.ProductSearchDto;
import com.windsome.dto.admin.OrderManagementDTO;
import com.windsome.entity.Member;
import com.windsome.entity.Order;
import com.windsome.entity.Product;
import com.windsome.entity.ProductImage;
import com.windsome.exception.AdminDeletionException;
import com.windsome.repository.board.qa.QaRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.repository.orderProduct.OrderProductRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.productImage.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final QaRepository qaRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * dashboard 정보 조회
     */
    @Transactional(readOnly = true)
    public DashboardInfoDto getDashboardData() {
        DashboardInfoDto dashboardInfoDto = new DashboardInfoDto();
        dashboardInfoDto.setTotalMembers(memberRepository.count());
        dashboardInfoDto.setTotalProducts(productRepository.count());
        dashboardInfoDto.setTotalQaPosts(qaRepository.count());
        dashboardInfoDto.setTotalSales(orderProductRepository.totalSales());
        dashboardInfoDto.setCategorySalesList(orderProductRepository.getCategorySalesCount().stream()
                .map(CategorySalesDto::new)
                .collect(Collectors.toList()));
        return dashboardInfoDto;
    }

    /**
     * 회원 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<MemberListResponseDTO> getMemberListForAdminPage(MemberListSearchDto memberListSearchDto, Pageable pageable) {
        return memberRepository.findMembersByCriteria(memberListSearchDto, pageable);
    }

    /**
     * 회원 상세 조회
     */
    @Transactional(readOnly = true)
    public AdminMemberDetailDTO getMemberDetails(Long accountId) {
        Member member = memberRepository.findById(accountId).orElseThrow(EntityNotFoundException::new);
        return AdminMemberDetailDTO.toDto(member);
    }

    /**
     * 회원 수정
     */
    public void updateMember(AdminMemberDetailDTO dto) throws Exception {
        Member member = memberRepository.findById(dto.getId()).orElseThrow(EntityNotFoundException::new);
        modelMapper.map(dto, member);
        // dto의 password 필드 값이 있으면 비밀번호 변경
        if (dto.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            member.setPassword(encodedPassword);
        }
        memberRepository.save(member);
    }

    /**
     * 회원 권한 수정
     */
    public void updateMemberRole(Long accountId, Role role) {
        Member account = memberRepository.findById(accountId).orElseThrow(EntityNotFoundException::new);
        account.setState(role);
        memberRepository.save(account);
    }

    /**
     * 회원 삭제
     */
    public void deleteMember(Long accountId) throws AdminDeletionException {
        Member member = memberRepository.findById(accountId).orElseThrow(EntityNotFoundException::new);
        if (member.getState().equals(Role.ADMIN)) {
            throw new AdminDeletionException("관리자 권한을 가진 사용자는 삭제할 수 없습니다.");
        }
        memberRepository.delete(member);
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
                    orderMngDto.setOrderProductDtoList(order.getOrderProducts().stream()
                            .map(orderProduct -> {
                                ProductImage productImage = productImageRepository.findByProductIdAndIsRepresentativeImage(orderProduct.getProduct().getId(), true);
                                return new OrderProductDto(orderProduct, productImage.getImageUrl());
                            })
                            .collect(Collectors.toList()));
                    return orderMngDto;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(orderMngDtoList, pageable, totalCount);
    }

    /**
     * 상품 조회
     */
    @Transactional(readOnly = true)
    public Page<Product> getProductList(ProductSearchDto productSearchDto, Pageable pageable) {
        return productRepository.findProducts(productSearchDto, pageable);
    }
}
