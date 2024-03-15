package com.windsome.repository.orderProduct;

import com.windsome.dto.admin.CategorySalesResult;
import com.windsome.entity.order.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    @Query(value = "select left(c.category_id, 1) as category, ifnull(sum(op.order_quantity), 0) as orderQuantity " +
            "from order_product op " +
            "join product p on p.product_id = op.product_id " +
            "right outer join category c on c.category_id = p.cate_id " +
            "group by category " +
            "order by category asc", nativeQuery = true)
    List<CategorySalesResult> getCategorySalesCount();

    List<OrderProduct> findByOrderId(Long orderId);
}
