package com.windsome.service.product;

import com.windsome.dto.product.InventoryDTO;
import com.windsome.repository.product.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<InventoryDTO> getInventories(Long productId) {
        return inventoryRepository.getInventoriesByProductId(productId);
    }

    public int getInventory(Long productId, Long colorId, Long sizeId) {
        return inventoryRepository.findByProductIdAndColorIdAndSizeId(productId, colorId, sizeId).getQuantity();
    }

}
