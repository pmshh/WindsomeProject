package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.entity.Account;
import com.windsome.service.CartService;
import com.windsome.service.ItemService;
import com.windsome.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;
    private final ReviewService reviewService;

    /**
     * 상품 상세 화면
     */
    @GetMapping("/item/{itemId}")
    public String itemDetail(@CurrentAccount Account account, Model model, Optional<Integer> page, @PathVariable("itemId") Long itemId) {
        Long cartItemTotalCount = null;
        if (account != null) {
            cartItemTotalCount = cartService.getCartItemTotalCount(account);
        }

        Pageable pageable = PageRequest.of(page.orElse(0), 5);
        model.addAttribute("reviews", reviewService.getItemDtlPageReviews(itemId, pageable));
        model.addAttribute("cartItemTotalCount", cartItemTotalCount);
        model.addAttribute("item", itemService.getItemFormDto(itemId));
        model.addAttribute("maxPage", 5);
        return "main/item/itemDtl";
    }
}
