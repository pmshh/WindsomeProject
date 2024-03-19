package com.windsome.service.order;

import com.windsome.entity.order.Payment;
import com.windsome.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * OrderService - Payment 저장
     */
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }
}
