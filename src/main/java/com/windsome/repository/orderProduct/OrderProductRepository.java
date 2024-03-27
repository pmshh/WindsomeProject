package com.windsome.repository.orderProduct;

import com.windsome.dto.admin.CategorySalesResult;
import com.windsome.entity.order.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    @Query(value = "select left(c.category_id, 1) as category, ifnull(sum(op.order_quantity), 0) as orderQuantity " +
            "from order_product op " +
            "join product p on p.product_id = op.product_id " +
            "right outer join category c on c.category_id = p.cate_id " +
            "where op.order_product_status NOT IN ('CANCELED', 'RETURNED', 'EXCHANGED') " +
            "group by category " +
            "order by category asc", nativeQuery = true)
    Optional<List<CategorySalesResult>> getCategorySalesCount();

    Optional<List<OrderProduct>> findByOrderId(Long orderId);
}
