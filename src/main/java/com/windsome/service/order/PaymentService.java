package com.windsome.service.order;

import com.windsome.entity.order.Payment;
import com.windsome.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 결제 저장
     */
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }

    /**
     * 총 결제 금액 조회
     */
    @Transactional(readOnly = true)
    public Long getTotalPaymentPrice() {
        return paymentRepository.getTotalPaymentPrice();
    }

    /**
     * 결제 조회
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByPaymentId(Long paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(EntityNotFoundException::new);
    }
}
