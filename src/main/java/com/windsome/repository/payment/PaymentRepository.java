package com.windsome.repository.payment;

import com.windsome.entity.order.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query(value = "select coalesce(sum(p.price),0) from Payment p")
    Long getTotalPaymentPrice();
}
