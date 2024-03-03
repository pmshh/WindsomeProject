package com.windsome.repository.order;

import com.windsome.constant.OrderStatus;
import com.windsome.dto.order.OrderDetailDTO;
import com.windsome.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Query("select COALESCE(sum(o.price),0) from Order o where o.member.id = :memberId")
    Long getTotalOrderAmountByMemberId(@Param("memberId") Long memberId);

    @Query("select new com.windsome.dto.order.OrderDetailDTO(" +
            "o.orderUid, o.orderDate, m.name, o.orderStatus, o.price, p.price, o.usedPoints, o.earnedPoints, o.name, o.zipcode, o.addr, o.addrDetail, o.tel, o.req)" +
            " from Order o join Payment p on p.id = o.payment.id join Member m on m.id = o.member.id where o.id = :orderId")
    OrderDetailDTO getOrderDetail(@Param("orderId") Long orderId);
}
