package com.windsome.controller.product;

import com.windsome.service.product.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/inventory")
    public ResponseEntity<Integer> getInventory(@RequestParam Long productId, @RequestParam Long colorId, @RequestParam Long sizeId) {
        try {
            int inventory = inventoryService.getStockQuantity(productId, colorId, sizeId);
            return ResponseEntity.ok().body(inventory);
        } catch (Exception e) {
            return ResponseEntity.ok().body(0);
        }
    }

}
