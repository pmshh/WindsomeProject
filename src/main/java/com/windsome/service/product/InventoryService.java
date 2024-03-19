package com.windsome.service.product;

import com.windsome.dto.product.InventoryDTO;
import com.windsome.entity.product.Inventory;
import com.windsome.repository.product.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * 재고 목록 조회
     * @return List<InventoryDTO>
     */
    public List<InventoryDTO> getInventories(Long productId) {
        return inventoryRepository.getInventoriesByProductId(productId);
    }

    /**
     * 특정 상품의 재고 수 조회
     * @return int
     */
    public int getStockQuantity(Long productId, Long colorId, Long sizeId) {
        return inventoryRepository.findByProductIdAndColorIdAndSizeId(productId, colorId, sizeId).getQuantity();
    }

    /**
     * 재고 조회
     * @return Inventory
     */
    public Inventory getInventoryByProductIdAndColorIdAndSizeId(Long productId, Long colorId, Long sizeId) {
        return inventoryRepository.findByProductIdAndColorIdAndSizeId(productId, colorId, sizeId);
    }

    /**
     * 재고 목록 조회
     * @return List<Inventory>
     */
    public List<Inventory> getInventoriesByProductId(Long productId) {
        return inventoryRepository.findAllByProductId(productId);
    }
}
