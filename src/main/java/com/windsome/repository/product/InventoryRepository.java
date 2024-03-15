package com.windsome.repository.product;

import com.windsome.dto.product.InventoryDTO;
import com.windsome.entity.product.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query(value = "select new com.windsome.dto.product.InventoryDTO(i.product.id, i.size.id, i.color.id, s.name, c.name, c.code, i.quantity) from Inventory i join Color c on i.color.id = c.id join Size s on i.size.id = s.id where i.product.id = :productId order by c.id asc, s.id asc")
    List<InventoryDTO> getInventoriesByProductId(@Param("productId") Long productId);

    void deleteAllByProductId(Long id);

    Inventory findByProductIdAndColorIdAndSizeId(Long productId, Long colorId, Long sizeId);

    List<Inventory> findAllByProductId(Long id);
}
