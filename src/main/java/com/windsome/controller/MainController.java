package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.dto.item.ItemSearchDto;
import com.windsome.entity.*;
import com.windsome.service.CartService;
import com.windsome.service.CategoryService;
import com.windsome.service.ItemService;
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

    private final ItemService itemService;
    private final CategoryService categoryService;
    private final CartService cartService;

    /**
     * 메인 화면
     */
    @GetMapping("/")
    public String home(@CurrentAccount Account account, ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 9);
        Long cartItemTotalCount = null;
        if (account != null) {
            cartItemTotalCount = cartService.getCartItemTotalCount(account);
        }
        model.addAttribute("cartItemTotalCount", cartItemTotalCount);
        model.addAttribute("items", itemService.getMainItemPage(itemSearchDto, pageable));
        model.addAttribute("categories", categoryService.getMainCategoryDto());
        model.addAttribute("itemSearchDto", itemSearchDto);
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
        return "account/login";
    }
}
