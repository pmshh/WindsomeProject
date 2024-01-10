package com.windsome.repository;

import com.windsome.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select COALESCE(sum(oi.count * oi.orderPrice), 0) from OrderItem oi join Order o on oi.order.id = o.id where o.orderStatus = 'READY'")
    Long totalSales();
}
