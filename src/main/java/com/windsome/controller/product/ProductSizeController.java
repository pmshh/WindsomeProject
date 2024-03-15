package com.windsome.controller.product;

import com.windsome.service.product.ProductColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sizes")
@RequiredArgsConstructor
public class ProductSizeController {

    private final ProductColorService productColorService;

}
