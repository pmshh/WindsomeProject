package com.windsome.controller.product;

import com.windsome.service.product.ProductColorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductColorController {

    private final ProductColorService productColorService;
}
