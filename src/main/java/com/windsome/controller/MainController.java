package com.windsome.controller;

import com.windsome.dto.product.ProductSearchDto;
import com.windsome.service.CartService;
import com.windsome.service.CategoryService;
import com.windsome.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProductService productService;
    private final CategoryService categoryService;

    /**
     * 메인 화면
     */
    @GetMapping("/")
    public String home(ProductSearchDto productSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 9);
        model.addAttribute("products", productService.getMainPageProducts(productSearchDto, pageable));
        model.addAttribute("categories", categoryService.fetchCategories());
        model.addAttribute("productSearchDto", productSearchDto);
        model.addAttribute("maxPage", 10);
        return "main/main";
    }

    /**
     * 로그인 화면
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception, Model model) {
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        return "member/login";
    }
}
