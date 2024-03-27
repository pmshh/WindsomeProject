package com.windsome.controller.main;

import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.service.main.MainService;
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

    private final MainService mainService;

    /**
     * 메인 화면
     */
    @GetMapping("/")
    public String home(ProductSearchDTO productSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 9);
        model.addAttribute("products", mainService.getMainPageProducts(productSearchDto, pageable));
        model.addAttribute("categories", mainService.getCategories());
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
