package com.windsome.repository.order;

import com.windsome.constant.OrderStatus;
import com.windsome.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o where o.member.userIdentifier = :userIdentifier order by o.orderDate desc")
    List<Order> findOrders(@Param("userIdentifier") String userIdentifier, Pageable pageable);

    @Query("select count(o) from Order o where o.member.userIdentifier = :userIdentifier")
    Long countOrder(@Param("userIdentifier") String userIdentifier);

    @Query("select o from Order o where o.member.userIdentifier like concat('%',:userIdentifier,'%') order by o.orderDate desc")
    List<Order> findOrderListForAdmin(@Param("userIdentifier") String userIdentifier, Pageable pageable);

    @Query("select count(o) from Order o where o.member.userIdentifier like concat('%',:userIdentifier,'%') order by o.orderDate desc")
    Long countOrderList(@Param("userIdentifier") String userIdentifier);

    Long countByMemberIdAndOrderStatus(Long id, OrderStatus orderStatus);
}