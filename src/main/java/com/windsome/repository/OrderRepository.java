package com.windsome.repository;

import com.windsome.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o where o.account.userIdentifier = :userIdentifier order by o.orderDate desc")
    List<Order> findOrders(@Param("userIdentifier") String userIdentifier, Pageable pageable);

    @Query("select count(o) from Order o where o.account.userIdentifier = :userIdentifier")
    Long countOrder(@Param("userIdentifier") String userIdentifier);
}
