package com.windsome.entity.order;

import com.windsome.constant.PaymentStatus;
import com.windsome.dto.order.OrderRequestDTO;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    private int price; // 결제 금액

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // 결제 상태

    private String paymentUid; // 결제 고유 번호

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Order order;

    /**
     * Constructors, Getters, Setters, etc.
     */
    public static Payment createPayment(OrderRequestDTO orderRequestDTO) {
        return Payment.builder()
                .price(orderRequestDTO.getTotalPaymentPrice())
                .status(PaymentStatus.PAYMENT_COMPLETED)
                .paymentUid(orderRequestDTO.getPaymentUid())
                .build();
    }
}
