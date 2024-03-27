package com.windsome.entity.order;

import com.windsome.constant.OrderStatus;
import com.windsome.constant.PaymentStatus;
import com.windsome.dto.order.OrderRequestDTO;
import com.windsome.entity.member.Member;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString(exclude = {"orderProducts", "payment"})
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 회원

    private String orderUid; // 주문 번호

    private LocalDateTime orderDate; // 주문 날짜

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문 현황

    private String name; // 받는분 성함

    private int price; // 총 주문 금액

    private String zipcode; // 우편 번호

    private String addr; // 받는분 주소

    private String addrDetail; // 상세 주소

    private String tel; // 받는분 전화 번호

    private String email; // 받는분 이메일

    private String req; // 배송 요청 사항

    private int earnedPoints; // 적립된 포인트 금액

    private int usedPoints; // 사용한 포인트 금액

    private int productCount; // 주문 상품 개수

    private String repProductName; // 대표 상품

    private String repProductImage; // 대표 상품 이미지

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>(); // 주문 상품 목록

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment; // 결제 정보

    /**
     * Constructors, Getters, Setters, etc.
     */
    public static Order createOrder(Member member, List<OrderProduct> orderProductList, OrderRequestDTO orderRequestDTO, Payment payment) {
        Order order = Order.builder()
                .member(member)
                .orderUid(orderRequestDTO.getOrderUid())
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.PROCESSING)
                .name(orderRequestDTO.getName())
                .price(orderRequestDTO.getTotalOrderPrice())
                .zipcode(orderRequestDTO.getZipcode())
                .addr(orderRequestDTO.getAddr())
                .addrDetail(orderRequestDTO.getAddrDetail())
                .tel(orderRequestDTO.getTel())
                .email(orderRequestDTO.getEmail())
                .req(orderRequestDTO.getReq())
                .earnedPoints(orderRequestDTO.getEarnedPoints())
                .usedPoints(orderRequestDTO.getUsedPoints())
                .productCount(orderRequestDTO.getProductCount())
                .repProductName(orderRequestDTO.getRepProductName())
                .repProductImage(orderRequestDTO.getRepProductImage())
                .build();
        order.setOrderProducts(orderProductList);
        order.setPayment(payment);
        return order;
    }

    // 연관 관계 설정 (Order, OrderProduct)
    public void setOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.setOrder(this);
        }
    }

    // 연관 관계 설정 (Order, Payment)
    public void setPayment(Payment payment) {
        this.payment = payment;
        payment.setOrder(this);
    }
}
