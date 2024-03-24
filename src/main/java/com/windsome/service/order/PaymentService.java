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

    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }

    public Long getTotalPaymentPrice() {
        return paymentRepository.getTotalPaymentPrice();
    }

    public Payment getPaymentByPaymentId(Long paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(EntityNotFoundException::new);
    }
}
