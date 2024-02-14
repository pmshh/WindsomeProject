package com.windsome.repository;

import com.windsome.dto.admin.NumOfSalesByCateDtoInterface;
import com.windsome.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select COALESCE(sum(oi.count * oi.price), 0) from OrderItem oi join Order o on oi.order.id = o.id where o.orderStatus = 'READY'")
    Long totalSales();

    @Query(value = "select left(c.id, 1) as category, ifnull(sum(oi.count), 0) as count " +
            "from order_item oi " +
            "join item i on i.item_id = oi.item_id " +
            "right outer join category c on c.id = i.cate_id " +
            "group by category " +
            "order by category asc", nativeQuery = true)
    List<NumOfSalesByCateDtoInterface> numberOfSalesByCategory();
}
