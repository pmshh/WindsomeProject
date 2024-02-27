package com.windsome.repository.orderProduct;

import com.windsome.dto.admin.CategorySalesResult;
import com.windsome.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    @Query("select COALESCE(sum(op.count * op.price), 0) from OrderProduct op join Order o on op.order.id = o.id where o.orderStatus = 'READY'")
    Long totalSales();

    @Query(value = "select left(c.category_id, 1) as category, ifnull(sum(op.count), 0) as count " +
            "from order_product op " +
            "join product p on p.product_id = op.product_id " +
            "right outer join category c on c.category_id = p.cate_id " +
            "group by category " +
            "order by category asc", nativeQuery = true)
    List<CategorySalesResult> getCategorySalesCount();
}
