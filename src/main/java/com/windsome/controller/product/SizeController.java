package com.windsome.controller.product;

import com.windsome.service.product.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sizes")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    @PostMapping("/add")
    public ResponseEntity<String> addProductSize(@RequestBody String[] sizeNames) {
        if (sizeService.existsByColorName(sizeNames)) {
            return ResponseEntity.badRequest().body("이미 존재하는 색상이 포함되어있습니다.");
        }
        sizeService.addProductSize(sizeNames);
        return ResponseEntity.status(HttpStatus.CREATED).body("Product size added successfully");
    }
}
