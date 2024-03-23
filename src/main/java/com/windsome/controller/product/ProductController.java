package com.windsome.controller.product;

import com.windsome.service.board.BoardService;
import com.windsome.service.product.InventoryService;
import com.windsome.service.product.ProductService;
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
    private final BoardService boardService;
    private final InventoryService inventoryService;

    /**
     * 상품 상세 화면
     */
    @GetMapping("/product/{productId}")
    public String showProductDetail(Optional<Integer> page, @PathVariable("productId") Long productId, RedirectAttributes redirectAttr, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 5);
        model.addAttribute("inventories", inventoryService.getInventories(productId));
        try {
            model.addAttribute("product", productService.getProductFormDto(productId));
            model.addAttribute("reviews", boardService.getProductReviewList(productId, pageable));
            model.addAttribute("maxPage", 5);
        } catch (EntityNotFoundException e) {
            redirectAttr.addFlashAttribute("message", "상품 정보를 불러오던 도중 오류가 발생하였습니다.");
            return "redirect:/";
        }
        return "main/product/product-detail";
    }
}
