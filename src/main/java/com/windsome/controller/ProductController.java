package com.windsome.controller;

import com.windsome.service.ProductService;
import com.windsome.service.board.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    /**
     * 상품 상세 화면
     */
    @GetMapping("/product/{productId}")
    public String showProductDetail(Model model, Optional<Integer> page, @PathVariable("productId") Long productId, RedirectAttributes redirectAttr) {
        Pageable pageable = PageRequest.of(page.orElse(0), 5);

        try {
            model.addAttribute("product", productService.getProductFormDto(productId));
            model.addAttribute("reviews", reviewService.getProductReviewList(productId, pageable));
            model.addAttribute("maxPage", 5);
        } catch (EntityNotFoundException e) {
            redirectAttr.addFlashAttribute("message", "상품 정보를 불러오던 도중 오류가 발생하였습니다.");
            return "redirect:/";
        }
        return "main/product/product-detail";
    }
}
